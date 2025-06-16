package io.github.cowwoc.canister.docker.main.parser;

import io.github.cowwoc.canister.core.exception.ResourceInUseException;
import io.github.cowwoc.canister.core.resource.CommandResult;
import io.github.cowwoc.canister.docker.api.client.DockerClient;
import io.github.cowwoc.canister.docker.exception.AlreadySwarmMemberException;
import io.github.cowwoc.canister.docker.exception.NotSwarmManagerException;
import io.github.cowwoc.canister.docker.id.NodeId;
import io.github.cowwoc.canister.docker.main.client.InternalDockerClient;
import io.github.cowwoc.canister.docker.resource.JoinToken;
import io.github.cowwoc.canister.docker.resource.Node.Role;
import io.github.cowwoc.canister.docker.resource.SwarmCreator.WelcomePackage;
import io.github.cowwoc.canister.docker.resource.SwarmLeaver;

import java.net.ConnectException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.cowwoc.canister.docker.main.parser.NodeParser.NOT_SWARM_MANAGER;
import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Parses responses to {@code Swarm} commands.
 */
public class SwarmParser extends DockerParser
{
	private static final Pattern CREATE_SWARM_PATTERN = Pattern.compile("""
		Swarm initialized: current node \\(([^)]+)\\) is now a manager\\.
		
		To add a worker to this swarm, run the following command:
		
		 *docker swarm join --token ([^ ]+) (.+?)
		
		To add a manager to this swarm, run 'docker swarm join-token manager' and follow the instructions\\.""");
	private static final Pattern JOIN_TOKEN_PATTERN = Pattern.compile("""
		To add a (?:manager|worker) to this swarm, run the following command:
		
		 *docker swarm join --token ([^ ]+) (.+?)
		""");
	private static final Pattern JOIN_SWARM_PATTERN = Pattern.compile(
		"This node joined a swarm as a (manager|worker)\\.");
	private static final String ALREADY_IN_SWARM = """
		Error response from daemon: This node is already part of a swarm. Use "docker swarm leave" to leave \
		this swarm and join another one.""";
	private static final Pattern CONNECTION_REFUSED = Pattern.compile("""
		Error response from daemon: rpc error: code = Unavailable desc = connection error: desc = "transport: \
		Error while dialing: dial (.+?): connect: connection refused\"""");
	private static final String INVALID_JOIN_TOKEN = "Error response from daemon: invalid join token";
	private static final String REMOVING_LAST_MANAGER = """
		Error response from daemon: You are attempting to leave the swarm on a node that is participating as a \
		manager. Removing the last manager erases all current state of the swarm. Use `--force` to ignore this \
		message.""";
	private static final Pattern REMOVING_QUORUM = Pattern.compile("""
		Error response from daemon: You are attempting to leave the swarm on a node that is participating as a \
		manager\\. Removing this node leaves \\d+ managers out of \\d+\\. Without a Raft quorum your swarm will \
		be inaccessible\\. The only way to restore a swarm that has lost consensus is to reinitialize it with \
		`--force-new-cluster`\\. Use `--force` to suppress this message\\.""");

	/**
	 * Creates a parser.
	 *
	 * @param client the client configuration
	 */
	public SwarmParser(InternalDockerClient client)
	{
		super(client);
	}

	/**
	 * Creates and joins a new swarm.
	 *
	 * @param result the result of executing a command
	 * @return the manager ID and the worker's join token
	 * @throws AlreadySwarmMemberException if this node is already a member of a swarm
	 */
	public WelcomePackage create(CommandResult result)
	{
		if (result.exitCode() != 0)
		{
			if (result.stderr().equals(ALREADY_IN_SWARM))
				throw new AlreadySwarmMemberException();
			throw result.unexpectedResponse();
		}
		Matcher matcher = CREATE_SWARM_PATTERN.matcher(result.stdout());
		if (!matcher.matches())
			throw result.unexpectedResponse();
		NodeId nodeId = NodeId.of(matcher.group(1));
		String token = matcher.group(2);
		InetSocketAddress managerAddress = toInetSocketAddress(matcher.group(3));
		JoinToken joinToken = new JoinToken(Role.WORKER, token, managerAddress);
		return new WelcomePackage(nodeId, joinToken);
	}

	/**
	 * Parses an {@code InetSocketAddress}.
	 *
	 * @param address the textual representation of the address
	 * @return the InetSocketAddress
	 * @throws NullPointerException     if {@code address} is null
	 * @throws IllegalArgumentException if {@code address}:
	 *                                  <ul>
	 *                                    <li>contains whitespace, or is empty.</li>
	 *                                    <li>is missing a port number.</li>
	 *                                  </ul>
	 */
	private InetSocketAddress toInetSocketAddress(String address)
	{
		requireThat(address, "address").doesNotContainWhitespace().isNotEmpty();

		// https://stackoverflow.com/a/2347356/14731
		URI uri = URI.create("tcp://" + address);
		if (uri.getPort() == -1 ||
			(uri.getHost() == null && InetAddress.ofLiteral(uri.getAuthority()) instanceof Inet6Address))
		{
			throw new IllegalArgumentException("Address must contain a port number.\n" +
				"Actual: " + address);
		}
		return InetSocketAddress.createUnresolved(uri.getHost(), uri.getPort());
	}

	/**
	 * Returns the secret value needed to join the swarm as a manager or a worker.
	 *
	 * @param result the result of executing a command
	 * @param role   the type of the join token
	 * @return the join token
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 */
	public JoinToken getJoinToken(CommandResult result, Role role)
	{
		if (result.exitCode() != 0)
		{
			if (result.stderr().startsWith(NOT_SWARM_MANAGER))
				throw new NotSwarmManagerException();
			throw result.unexpectedResponse();
		}
		Matcher matcher = JOIN_TOKEN_PATTERN.matcher(result.stdout());
		if (!matcher.matches())
			throw result.unexpectedResponse();
		InetSocketAddress managerAddress = toInetSocketAddress(matcher.group(2));
		return new JoinToken(role, matcher.group(1), managerAddress);
	}

	/**
	 * Joins an existing swarm.
	 *
	 * @param result the result of executing a command
	 * @return the type of the current node
	 * @throws IllegalArgumentException    if the join token is invalid
	 * @throws AlreadySwarmMemberException if this node is already a member of a swarm
	 * @throws ConnectException            if the {@link DockerClient#getClientContext() referenced context}
	 *                                     referenced a TCP/IP socket that refused a connection
	 */
	public Role join(CommandResult result) throws ConnectException
	{
		if (result.exitCode() != 0)
		{
			String stderr = result.stderr();
			if (stderr.equals(ALREADY_IN_SWARM))
				throw new AlreadySwarmMemberException();
			Matcher matcher = CONNECTION_REFUSED.matcher(stderr);
			if (matcher.matches())
				throw new ConnectException("Connection refused: " + matcher.group(1));
			if (stderr.equals(INVALID_JOIN_TOKEN))
				throw new IllegalArgumentException("Invalid join token");
			throw result.unexpectedResponse();
		}
		Matcher matcher = JOIN_SWARM_PATTERN.matcher(result.stdout());
		if (!matcher.matches())
			throw result.unexpectedResponse();
		return Role.valueOf(matcher.group(1).toUpperCase(Locale.ROOT));
	}

	/**
	 * Leaves a swarm.
	 *
	 * @param result the result of executing a command
	 * @throws ResourceInUseException if the node is a manager and {@link SwarmLeaver#force()} was not used. The
	 *                                safe way to remove a manager from a swarm is to demote it to a worker and
	 *                                then direct it to leave the quorum without using {@code force}. Only use
	 *                                {@code force} in situations where the swarm will no longer be used after
	 *                                the manager leaves, such as in a single-node swarm.
	 */
	public void leave(CommandResult result) throws ResourceInUseException
	{
		if (result.exitCode() != 0)
		{
			String stderr = result.stderr();
			if (stderr.equals(REMOVING_LAST_MANAGER) || REMOVING_QUORUM.matcher(stderr).matches())
			{
				throw new ResourceInUseException("To safely remove this manager from the swarm, first demote it " +
					"to a worker, then leave the quorum. If you intend to remove the final manager and erase the " +
					"swarm's state, use SwarmLeaver.force().\n" +
					"Cause: " + stderr);
			}
			throw result.unexpectedResponse();
		}
		if (!result.stdout().equals("Node left the swarm."))
			throw result.unexpectedResponse();
	}
}