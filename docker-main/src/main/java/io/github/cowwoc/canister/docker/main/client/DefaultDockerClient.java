package io.github.cowwoc.canister.docker.main.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.cowwoc.canister.buildx.internal.client.AbstractBuildXClient;
import io.github.cowwoc.canister.core.exception.ResourceNotFoundException;
import io.github.cowwoc.canister.core.id.ImageId;
import io.github.cowwoc.canister.core.internal.client.CommandRunner;
import io.github.cowwoc.canister.core.internal.util.Lists;
import io.github.cowwoc.canister.core.internal.util.Paths;
import io.github.cowwoc.canister.core.resource.CommandResult;
import io.github.cowwoc.canister.core.spi.util.ParameterValidator;
import io.github.cowwoc.canister.docker.api.client.DockerClient;
import io.github.cowwoc.canister.docker.exception.NotSwarmManagerException;
import io.github.cowwoc.canister.docker.id.ConfigId;
import io.github.cowwoc.canister.docker.id.ContainerId;
import io.github.cowwoc.canister.docker.id.ContextId;
import io.github.cowwoc.canister.docker.id.NetworkId;
import io.github.cowwoc.canister.docker.id.NodeId;
import io.github.cowwoc.canister.docker.id.ServiceId;
import io.github.cowwoc.canister.docker.id.TaskId;
import io.github.cowwoc.canister.docker.main.parser.ConfigParser;
import io.github.cowwoc.canister.docker.main.parser.ContainerParser;
import io.github.cowwoc.canister.docker.main.parser.ContextParser;
import io.github.cowwoc.canister.docker.main.parser.ImageParser;
import io.github.cowwoc.canister.docker.main.parser.NetworkParser;
import io.github.cowwoc.canister.docker.main.parser.NodeParser;
import io.github.cowwoc.canister.docker.main.parser.ServiceParser;
import io.github.cowwoc.canister.docker.main.parser.SwarmParser;
import io.github.cowwoc.canister.docker.main.resource.DefaultConfigCreator;
import io.github.cowwoc.canister.docker.main.resource.DefaultContainerCreator;
import io.github.cowwoc.canister.docker.main.resource.DefaultContainerLogs;
import io.github.cowwoc.canister.docker.main.resource.DefaultContainerRemover;
import io.github.cowwoc.canister.docker.main.resource.DefaultContainerStarter;
import io.github.cowwoc.canister.docker.main.resource.DefaultContainerStopper;
import io.github.cowwoc.canister.docker.main.resource.DefaultContextCreator;
import io.github.cowwoc.canister.docker.main.resource.DefaultContextRemover;
import io.github.cowwoc.canister.docker.main.resource.DefaultDockerImageBuilder;
import io.github.cowwoc.canister.docker.main.resource.DefaultImagePuller;
import io.github.cowwoc.canister.docker.main.resource.DefaultImagePusher;
import io.github.cowwoc.canister.docker.main.resource.DefaultImageRemover;
import io.github.cowwoc.canister.docker.main.resource.DefaultNodeRemover;
import io.github.cowwoc.canister.docker.main.resource.DefaultServiceCreator;
import io.github.cowwoc.canister.docker.main.resource.DefaultSwarmCreator;
import io.github.cowwoc.canister.docker.main.resource.DefaultSwarmJoiner;
import io.github.cowwoc.canister.docker.main.resource.DefaultSwarmLeaver;
import io.github.cowwoc.canister.docker.resource.Config;
import io.github.cowwoc.canister.docker.resource.ConfigCreator;
import io.github.cowwoc.canister.docker.resource.ConfigElement;
import io.github.cowwoc.canister.docker.resource.Container;
import io.github.cowwoc.canister.docker.resource.ContainerCreator;
import io.github.cowwoc.canister.docker.resource.ContainerElement;
import io.github.cowwoc.canister.docker.resource.ContainerLogs;
import io.github.cowwoc.canister.docker.resource.ContainerRemover;
import io.github.cowwoc.canister.docker.resource.ContainerStarter;
import io.github.cowwoc.canister.docker.resource.ContainerStopper;
import io.github.cowwoc.canister.docker.resource.Context;
import io.github.cowwoc.canister.docker.resource.ContextCreator;
import io.github.cowwoc.canister.docker.resource.ContextElement;
import io.github.cowwoc.canister.docker.resource.ContextEndpoint;
import io.github.cowwoc.canister.docker.resource.ContextRemover;
import io.github.cowwoc.canister.docker.resource.DockerImage;
import io.github.cowwoc.canister.docker.resource.DockerImageBuilder;
import io.github.cowwoc.canister.docker.resource.DockerImageElement;
import io.github.cowwoc.canister.docker.resource.ImagePuller;
import io.github.cowwoc.canister.docker.resource.ImagePusher;
import io.github.cowwoc.canister.docker.resource.ImageRemover;
import io.github.cowwoc.canister.docker.resource.JoinToken;
import io.github.cowwoc.canister.docker.resource.Network;
import io.github.cowwoc.canister.docker.resource.NetworkElement;
import io.github.cowwoc.canister.docker.resource.Node;
import io.github.cowwoc.canister.docker.resource.Node.Role;
import io.github.cowwoc.canister.docker.resource.NodeElement;
import io.github.cowwoc.canister.docker.resource.NodeRemover;
import io.github.cowwoc.canister.docker.resource.Service;
import io.github.cowwoc.canister.docker.resource.ServiceCreator;
import io.github.cowwoc.canister.docker.resource.ServiceElement;
import io.github.cowwoc.canister.docker.resource.SwarmCreator;
import io.github.cowwoc.canister.docker.resource.SwarmJoiner;
import io.github.cowwoc.canister.docker.resource.SwarmLeaver;
import io.github.cowwoc.canister.docker.resource.Task;
import io.github.cowwoc.pouch.core.ConcurrentLazyReference;
import io.github.cowwoc.pouch.core.WrappedCheckedException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope.ShutdownOnFailure;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static java.nio.charset.StandardCharsets.UTF_8;

@SuppressWarnings("PMD.MoreThanOneLogger")
public final class DefaultDockerClient extends AbstractBuildXClient
	implements InternalDockerClient
{
	private static final ConcurrentLazyReference<Path> EXECUTABLE_FROM_PATH = ConcurrentLazyReference.create(
		() ->
		{
			Path path = Paths.searchPath(List.of("docker"));
			if (path == null)
				throw new UncheckedIOException(new IOException("Could not find docker on the PATH"));
			return path;
		});

	/**
	 * @return the path of the {@code docker} executable located in the {@code PATH} environment variable
	 */
	private static Path getExecutableFromPath() throws IOException
	{
		try
		{
			return EXECUTABLE_FROM_PATH.getValue();
		}
		catch (UncheckedIOException e)
		{
			throw e.getCause();
		}
	}

	/**
	 * The exit code returned by Docker in response to SIGTERM.
	 */
	private static final int SIGTERM = 143;
	private ContextId clientContext;
	@SuppressWarnings("this-escape")
	private final ConfigParser configParser = new ConfigParser(this);
	@SuppressWarnings("this-escape")
	private final ContainerParser containerParser = new ContainerParser(this);
	@SuppressWarnings("this-escape")
	private final ImageParser imageParser = new ImageParser(this);
	@SuppressWarnings("this-escape")
	private final ContextParser contextParser = new ContextParser(this);
	@SuppressWarnings("this-escape")
	private final NetworkParser networkParser = new NetworkParser(this);
	@SuppressWarnings("this-escape")
	private final NodeParser nodeParser = new NodeParser(this);
	@SuppressWarnings("this-escape")
	private final ServiceParser serviceParser = new ServiceParser(this);
	@SuppressWarnings("this-escape")
	private final SwarmParser swarmParser = new SwarmParser(this);

	/**
	 * Returns a client.
	 *
	 * @param executable the path of the Docker client
	 * @throws NullPointerException     if {@code executable} is null
	 * @throws IllegalArgumentException if the path referenced by {@code executable} does not exist or is not a
	 *                                  file
	 * @throws IOException              if an I/O error occurs while reading {@code executable}'s attributes
	 */
	public DefaultDockerClient(Path executable) throws IOException
	{
		super(executable);
	}

	@Override
	public DockerClient retryTimeout(Duration duration)
	{
		return (DockerClient) super.retryTimeout(duration);
	}

	@Override
	public ProcessBuilder getProcessBuilder(List<String> arguments)
	{
		List<String> command = new ArrayList<>(arguments.size() + 3);
		command.add(executable.toString());
		if (clientContext != null)
		{
			command.add("--context");
			command.add(clientContext.getValue());
		}
		command.addAll(arguments);
		return new ProcessBuilder(command);
	}

	@Override
	public DockerImageBuilder buildImage()
	{
		return new DefaultDockerImageBuilder(this);
	}

	@Override
	public ConfigParser getConfigParser()
	{
		return configParser;
	}

	@Override
	public ContainerParser getContainerParser()
	{
		return containerParser;
	}

	@Override
	public ImageParser getImageParser()
	{
		return imageParser;
	}

	@Override
	public ContextParser getContextParser()
	{
		return contextParser;
	}

	@Override
	public NetworkParser getNetworkParser()
	{
		return networkParser;
	}

	@Override
	public NodeParser getNodeParser()
	{
		return nodeParser;
	}

	@Override
	public ServiceParser getServiceParser()
	{
		return serviceParser;
	}

	@Override
	public SwarmParser getSwarmParser()
	{
		return swarmParser;
	}

	@Override
	public DockerClient login(String username, String password)
		throws IOException, InterruptedException
	{
		// https://docs.docker.com/reference/cli/docker/login/
		List<String> arguments = List.of("login", "--username", username, "--password-stdin");
		retry(_ -> run(arguments, ByteBuffer.wrap(password.getBytes(UTF_8))));
		return this;
	}

	@Override
	public DockerClient login(String username, String password, String serverAddress)
		throws IOException, InterruptedException
	{
		requireThat(username, "username").doesNotContainWhitespace().isNotEmpty();
		requireThat(password, "password").doesNotContainWhitespace().isNotEmpty();
		requireThat(serverAddress, "serverAddress").doesNotContainWhitespace().isNotEmpty();

		// https://docs.docker.com/reference/cli/docker/login/
		List<String> arguments = List.of("login", "--username", username, "--password-stdin", serverAddress);
		retry(_ -> run(arguments, ByteBuffer.wrap(password.getBytes(UTF_8))));
		return this;
	}

	@Override
	public List<Config> getConfigs() throws IOException, InterruptedException
	{
		return getConfigs(_ -> true);
	}

	@Override
	public List<Config> getConfigs(Predicate<ConfigElement> predicate) throws IOException, InterruptedException
	{
		// https://docs.docker.com/reference/cli/docker/config/ls/
		List<String> arguments = List.of("config", "ls", "--format", "json");
		CommandResult result = retry(_ -> run(arguments));
		List<Config> configs = new ArrayList<>();
		for (ConfigElement match : getConfigParser().list(result).stream().filter(predicate).toList())
			configs.add(getConfig(match.id()));
		return configs;
	}

	@Override
	public Config getConfig(String id) throws IOException, InterruptedException
	{
		return getConfig(ConfigId.of(id));
	}

	@Override
	public Config getConfig(ConfigId id) throws IOException, InterruptedException
	{
		// https://docs.docker.com/reference/cli/docker/config/inspect/
		List<String> arguments = List.of("config", "inspect", id.getValue());
		CommandResult result = retry(_ -> run(arguments));
		return getConfigParser().configFromServer(result);
	}

	@Override
	public ConfigCreator createConfig()
	{
		return new DefaultConfigCreator(this);
	}

	@Override
	public List<Container> getContainers() throws IOException, InterruptedException
	{
		return getContainers(_ -> true);
	}

	@Override
	public List<Container> getContainers(Predicate<ContainerElement> predicate)
		throws IOException, InterruptedException
	{
		// https://docs.docker.com/reference/cli/docker/container/ls/
		List<String> arguments = List.of("container", "ls", "--format", "json", "--all", "--no-trunc");
		CommandResult result = retry(_ -> run(arguments));
		List<Container> containers = new ArrayList<>();
		for (ContainerElement match : getContainerParser().list(result).stream().filter(predicate).toList())
			containers.add(getContainer(match.id()));
		return containers;
	}

	@Override
	public Container getContainer(String id) throws IOException, InterruptedException
	{
		return getContainer(ContainerId.of(id));
	}

	@Override
	public Container getContainer(ContainerId id) throws IOException, InterruptedException
	{
		requireThat(id, "id").isNotNull();

		// https://docs.docker.com/reference/cli/docker/container/inspect/
		List<String> arguments = List.of("container", "inspect", id.getValue());
		CommandResult result = retry(_ -> run(arguments));
		return getContainerParser().configFromServer(result);
	}

	@Override
	public ContainerCreator createContainer(String imageId)
	{
		return createContainer(ImageId.of(imageId));
	}

	@Override
	public ContainerCreator createContainer(ImageId imageId)
	{
		return new DefaultContainerCreator(this, imageId);
	}

	@Override
	public DockerClient renameContainer(String id, String newName)
		throws IOException, InterruptedException
	{
		return renameContainer(ContainerId.of(id), newName);
	}

	@Override
	public DockerClient renameContainer(ContainerId id, String newName)
		throws IOException, InterruptedException
	{
		requireThat(id, "id").isNotNull();
		ParameterValidator.validateName(newName, "newName");

		// https://docs.docker.com/reference/cli/docker/container/rename/
		List<String> arguments = List.of("container", "rename", id.getValue(), newName);
		CommandResult result = retry(_ -> run(arguments));
		getContainerParser().rename(result);
		return this;
	}

	@Override
	public ContainerStarter startContainer(String id)
	{
		return startContainer(ContainerId.of(id));
	}

	@Override
	public ContainerStarter startContainer(ContainerId id)
	{
		return new DefaultContainerStarter(this, id);
	}

	@Override
	public ContainerStopper stopContainer(String id)
	{
		return stopContainer(ContainerId.of(id));
	}

	@Override
	public ContainerStopper stopContainer(ContainerId id)
	{
		return new DefaultContainerStopper(this, id);
	}

	@Override
	public ContainerRemover removeContainer(String id)
	{
		return removeContainer(ContainerId.of(id));
	}

	@Override
	public ContainerRemover removeContainer(ContainerId id)
	{
		return new DefaultContainerRemover(this, id);
	}

	@Override
	public int waitUntilContainerStops(String id) throws IOException, InterruptedException
	{
		return waitUntilContainerStops(ContainerId.of(id));
	}

	@Override
	public int waitUntilContainerStops(ContainerId id) throws IOException, InterruptedException
	{
		requireThat(id, "id").isNotNull();

		// https://docs.docker.com/reference/cli/docker/container/wait/
		List<String> arguments = List.of("container", "wait", id.getValue());
		CommandResult result = retry(_ -> run(arguments));
		return getContainerParser().waitUntilStopped(result);
	}

	@Override
	public void waitUntilContainerStatus(Container.Status status, String id)
		throws IOException, InterruptedException
	{
		waitUntilContainerStatus(status, ContainerId.of(id));
	}

	@Override
	public Container waitUntilContainerStatus(Container.Status status, ContainerId id)
		throws IOException, InterruptedException
	{
		requireThat(status, "status").isNotNull();
		requireThat(id, "id").isNotNull();

		Container container = getContainer(id);
		if (container == null)
			throw new ResourceNotFoundException("Container " + id);
		if (container.getStatus().equals(status))
			return container;
		switch (status)
		{
			case CREATED -> waitUntilContainerEvent(container, status, "create");
			case RUNNING -> waitUntilContainerEvent(container, status, "start");
			case PAUSED -> waitUntilContainerEvent(container, status, "pause");
			case RESTARTING -> waitUntilContainerEvents(Set.of("die", "start"), new Predicate<>()
			{
				private final List<String> pending = new ArrayList<>(List.of("die", "start"));

				@Override
				public boolean test(String line)
				{
					try
					{
						JsonNode json = getJsonMapper().readTree(line);
						String id = json.get("id").textValue();
						if (!container.getId().getValue().equals(id))
							return false;
						String event = json.get("status").textValue();
						if (event.equals(pending.getFirst()))
							pending.removeFirst();
						return pending.isEmpty() || container.reload().getStatus().equals(status);
					}
					catch (IOException | InterruptedException e)
					{
						throw WrappedCheckedException.wrap(e);
					}
				}
			});
			case EXITED -> waitUntilContainerEvent(container, status, "die");
			case REMOVING -> waitUntilContainerEvent(container, status, "destroy");
			case DEAD -> waitUntilContainerEvents(container, status, Set.of("die", "oom"));
		}
		return container.reload();
	}

	/**
	 * Waits for container events.
	 *
	 * @param container the container to monitor
	 * @param status    the status to wait for
	 * @param event     the event to wait for
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	private void waitUntilContainerEvent(Container container, Container.Status status, String event)
		throws IOException, InterruptedException
	{
		waitUntilContainerEvents(container, status, Set.of(event));
	}

	/**
	 * Waits for container events.
	 *
	 * @param container the container to monitor
	 * @param status    the status to wait for
	 * @param events    the events to wait for
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	private void waitUntilContainerEvents(Container container, Container.Status status, Set<String> events)
		throws IOException, InterruptedException
	{
		JsonMapper jsonMapper = getJsonMapper();
		waitUntilContainerEvents(events, line ->
		{
			try
			{
				JsonNode json = jsonMapper.readTree(line);
				String id = json.get("id").textValue();
				if (!container.getId().getValue().equals(id))
					return false;
				String event = json.get("status").textValue();
				return events.contains(event) || container.reload().getStatus().equals(status);
			}
			catch (IOException | InterruptedException e)
			{
				throw WrappedCheckedException.wrap(e);
			}
		});
	}

	/**
	 * Waits for container events.
	 *
	 * @param events            the events to wait for
	 * @param terminateOnStdout a function that consumes stdout lines and returns {@code true} if the process
	 *                          should be terminated
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	private void waitUntilContainerEvents(Set<String> events, Predicate<String> terminateOnStdout)
		throws IOException, InterruptedException
	{
		// https://docs.docker.com/reference/cli/docker/system/events/
		List<String> arguments = new ArrayList<>(5 + events.size());
		Collections.addAll(arguments, "system", "events", "--filter");
		for (String event : events)
			arguments.add("event=" + event);
		Collections.addAll(arguments, "--format", "json");

		CommandRunner commandRunner = new CommandRunner(getProcessBuilder(arguments)).
			terminateOnStdout(terminateOnStdout).
			failureHandler(result ->
			{
				if (result.exitCode() == SIGTERM)
					return;
				commandFailed(result);
			});
		retry(_ -> commandRunner.apply());
	}

	@Override
	public ContainerLogs getContainerLogs(String id)
	{
		return getContainerLogs(ContainerId.of(id));
	}

	@Override
	public ContainerLogs getContainerLogs(ContainerId id)
	{
		return new DefaultContainerLogs(this, id);
	}

	@Override
	public List<Context> getContexts() throws IOException, InterruptedException
	{
		return getContexts(_ -> true);
	}

	@Override
	public List<Context> getContexts(Predicate<ContextElement> predicate)
		throws IOException, InterruptedException
	{
		// https://docs.docker.com/reference/cli/docker/context/ls/
		List<String> arguments = List.of("context", "ls", "--format", "json");
		return retry(_ ->
		{
			CommandResult result = run(arguments);
			try
			{
				List<Context> contexts = new ArrayList<>();
				for (ContextElement match : getContextParser().list(result).stream().filter(predicate).toList())
				{
					Context context = getContext(match.id());
					if (context == null)
					{
						// The context is not fully initialized, skip it.
						continue;
					}
					contexts.add(context);
				}
				return contexts;
			}
			catch (IllegalArgumentException e)
			{
				// Sometimes the context "endpoint" and "error" are both empty strings. The error gets populated
				// a bit later.
				throw new IOException(e);
			}
		});
	}

	@Override
	public Context getContext(String id) throws IOException, InterruptedException
	{
		return getContext(ContextId.of(id));
	}

	@Override
	public Context getContext(ContextId id) throws IOException, InterruptedException
	{
		requireThat(id, "id").isNotNull();

		// https://docs.docker.com/reference/cli/docker/context/inspect/
		List<String> arguments = List.of("context", "inspect", id.getValue());
		CommandResult result = retry(_ -> run(arguments));
		return getContextParser().getContext(result);
	}

	@Override
	public ContextCreator createContext(String name, ContextEndpoint endpoint)
	{
		return new DefaultContextCreator(this, name, endpoint);
	}

	@Override
	public ContextRemover removeContext(String name)
	{
		return removeContext(ContextId.of(name));
	}

	@Override
	public ContextRemover removeContext(ContextId id)
	{
		return new DefaultContextRemover(this, id);
	}

	@Override
	public ContextId getClientContext()
	{
		return clientContext;
	}

	@Override
	public DockerClient setClientContext(String id)
	{
		ContextId contextId;
		if (id.isEmpty())
			contextId = null;
		else
			contextId = ContextId.of(id);
		return setClientContext(contextId);
	}

	@Override
	public DockerClient setClientContext(ContextId id)
	{
		this.clientContext = id;
		return this;
	}

	@Override
	public ContextId getUserContext() throws IOException, InterruptedException
	{
		// https://docs.docker.com/reference/cli/docker/context/show/
		List<String> arguments = List.of("context", "show");
		CommandResult result = retry(_ -> run(arguments));
		return getContextParser().show(result);
	}

	@Override
	public DockerClient setUserContext(String id) throws IOException, InterruptedException
	{
		return setUserContext(ContextId.of(id));
	}

	@Override
	public DockerClient setUserContext(ContextId id) throws IOException, InterruptedException
	{
		requireThat(id, "id").isNotNull();

		// https://docs.docker.com/reference/cli/docker/context/use/
		List<String> arguments = List.of("context", "use", id.getValue());
		CommandResult result = retry(_ -> run(arguments));
		getContextParser().use(result);
		return this;
	}

	@Override
	public List<ImageId> getImageIds() throws IOException, InterruptedException
	{
		return getImageIds(_ -> true);
	}

	@Override
	public List<ImageId> getImageIds(Predicate<DockerImageElement> predicate)
		throws IOException, InterruptedException
	{
		// https://docs.docker.com/reference/cli/docker/image/ls/
		List<String> arguments = List.of("image", "ls", "--format", "json", "--all", "--digests", "--no-trunc");
		CommandResult result = retry(_ -> run(arguments));
		List<ImageId> ids = new ArrayList<>();
		for (DockerImageElement match : getImageParser().list(result).stream().filter(predicate).toList())
			ids.add(match.id());
		return ids;
	}

	@Override
	public List<DockerImage> getImages() throws IOException, InterruptedException
	{
		return getImages(_ -> true);
	}

	@Override
	public List<DockerImage> getImages(Predicate<DockerImageElement> predicate)
		throws IOException, InterruptedException
	{
		List<DockerImage> images = new ArrayList<>();
		for (ImageId id : getImageIds())
			images.add(getImage(id));
		return images;
	}

	@Override
	public DockerImage getImage(String id) throws IOException, InterruptedException
	{
		return (DockerImage) super.getImage(id);
	}

	@Override
	public DockerImage getImage(ImageId id) throws IOException, InterruptedException
	{
		requireThat(id, "id").isNotNull();

		// https://docs.docker.com/reference/cli/docker/image/inspect/
		List<String> arguments = List.of("image", "inspect", "--format", "json", id.getValue());
		CommandResult result = retry(_ -> run(arguments));
		return getImageParser().imageFromServer(result);
	}

	@Override
	public void tagImage(String id, String target) throws IOException, InterruptedException
	{
		tagImage(ImageId.of(id), target);
	}

	@Override
	public void tagImage(ImageId id, String target) throws IOException, InterruptedException
	{
		requireThat(id, "id").isNotNull();
		ParameterValidator.validateImageReference(target, "target");

		// https://docs.docker.com/reference/cli/docker/image/tag/
		List<String> arguments = List.of("image", "tag", id.getValue(), target);
		CommandResult result = retry(_ -> run(arguments));
		getImageParser().tag(result);
	}

	@Override
	public ImagePuller pullImage(String reference)
	{
		return new DefaultImagePuller(this, reference);
	}

	@Override
	public ImagePusher pushImage(String reference)
	{
		return new DefaultImagePusher(this, reference);
	}

	@Override
	public ImageRemover removeImageTag(String reference)
	{
		return new DefaultImageRemover(this, ImageId.of(reference));
	}

	@Override
	public ImageRemover removeImage(String id)
	{
		return removeImage(ImageId.of(id));
	}

	@Override
	public ImageRemover removeImage(ImageId id)
	{
		return new DefaultImageRemover(this, id);
	}

	@Override
	public SwarmCreator createSwarm()
	{
		return new DefaultSwarmCreator(this);
	}

	@Override
	public SwarmJoiner joinSwarm()
	{
		return new DefaultSwarmJoiner(this);
	}

	@Override
	public SwarmLeaver leaveSwarm()
	{
		return new DefaultSwarmLeaver(this);
	}

	@Override
	public JoinToken getManagerJoinToken() throws IOException, InterruptedException
	{
		// https://docs.docker.com/reference/cli/docker/swarm/join-token/
		List<String> arguments = List.of("swarm", "join-token", "manager");
		CommandResult result = retry(_ -> run(arguments));
		return getSwarmParser().getJoinToken(result, Role.MANAGER);
	}

	@Override
	public JoinToken getWorkerJoinToken() throws IOException, InterruptedException
	{
		// https://docs.docker.com/reference/cli/docker/swarm/join-token/
		List<String> arguments = List.of("swarm", "join-token", "worker");
		CommandResult result = retry(_ -> run(arguments));
		return getSwarmParser().getJoinToken(result, Role.WORKER);
	}

	@Override
	public List<Network> getNetworks() throws IOException, InterruptedException
	{
		return getNetworks(_ -> true);
	}

	@Override
	public List<Network> getNetworks(Predicate<NetworkElement> predicate)
		throws IOException, InterruptedException
	{
		// https://docs.docker.com/reference/cli/docker/network/ls/
		List<String> arguments = List.of("network", "ls", "--format", "json", "--no-trunc");
		CommandResult result = retry(_ -> run(arguments));
		List<Network> networks = new ArrayList<>();
		for (NetworkElement match : getNetworkParser().list(result).stream().filter(predicate).toList())
			networks.add(getNetwork(match.id()));
		return networks;
	}

	@Override
	public Network getNetwork(String id) throws IOException, InterruptedException
	{
		return getNetwork(NetworkId.of(id));
	}

	@Override
	public Network getNetwork(NetworkId id) throws IOException, InterruptedException
	{
		requireThat(id, "id").isNotNull();

		// https://docs.docker.com/reference/cli/docker/network/inspect/
		List<String> arguments = List.of("network", "inspect", id.getValue());
		CommandResult result = retry(_ -> run(arguments));
		return getNetworkParser().networkFromServer(result);
	}

	@Override
	public List<Node> getNodes() throws IOException, InterruptedException
	{
		return getNodes(_ -> true);
	}

	@Override
	public List<Node> getNodes(Predicate<NodeElement> predicate) throws IOException, InterruptedException
	{
		List<Node> nodes = new ArrayList<>();
		for (NodeElement match : getNodes(List.of()).stream().filter(predicate).toList())
			nodes.add(getNode(match.id()));
		return nodes;
	}

	/**
	 * Returns nodes that match the specified filters.
	 *
	 * @param filters the filters to apply to the list
	 * @return the matching nodes
	 * @throws NullPointerException     if {@code filters} is null
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	private List<NodeElement> getNodes(List<String> filters) throws IOException, InterruptedException
	{
		assert filters != null;

		// https://docs.docker.com/reference/cli/docker/node/ls/
		List<String> arguments = new ArrayList<>(4 + filters.size() * 2);
		arguments.add("node");
		arguments.add("ls");
		arguments.add("--format");
		arguments.add("json");
		for (String filter : filters)
		{
			arguments.add("--filter");
			arguments.add(filter);
		}
		CommandResult result = retry(_ -> run(arguments));
		return getNodeParser().listNodes(result);
	}

	@Override
	public List<NodeElement> listManagerNodes() throws IOException, InterruptedException
	{
		return getNodes(List.of("role=manager"));
	}

	@Override
	public List<NodeElement> listWorkerNodes() throws IOException, InterruptedException
	{
		return getNodes(List.of("role=worker"));
	}

	@Override
	public NodeId getCurrentNodeId() throws IOException, InterruptedException
	{
		// https://docs.docker.com/reference/cli/docker/system/info/
		List<String> arguments = new ArrayList<>(4);
		arguments.add("system");
		arguments.add("info");
		arguments.add("--format");
		arguments.add("{{json .Swarm.NodeID}}");
		CommandResult result = retry(_ -> run(arguments));
		return getNodeParser().getNodeId(result);
	}

	@Override
	public Node getCurrentNode() throws IOException, InterruptedException
	{
		return getNode(getCurrentNodeId());
	}

	@Override
	public Node getNode(String id) throws IOException, InterruptedException
	{
		return getNode(NodeId.of(id));
	}

	@Override
	public Node getNode(NodeId id) throws IOException, InterruptedException
	{
		requireThat(id, "id").isNotNull();

		// https://docs.docker.com/reference/cli/docker/node/inspect/
		List<String> arguments = List.of("node", "inspect", id.getValue());
		CommandResult result = retry(_ -> run(arguments));
		return getNodeParser().getNode(result);
	}

	@Override
	public List<Task> getTasksByNode() throws IOException, InterruptedException
	{
		// https://docs.docker.com/reference/cli/docker/node/ps/
		List<String> arguments = new ArrayList<>(5);
		arguments.add("node");
		arguments.add("ps");
		arguments.add("--format");
		arguments.add("json");
		arguments.add("--no-trunc");
		CommandResult result = retry(_ -> run(arguments));
		return getNodeParser().listTasksByNode(result);
	}

	@Override
	public List<Task> getTasksByNode(String id) throws IOException, InterruptedException
	{
		return getTasksByNode(NodeId.of(id));
	}

	@Override
	public List<Task> getTasksByNode(NodeId id) throws IOException, InterruptedException
	{
		requireThat(id, "id").isNotNull();

		// https://docs.docker.com/reference/cli/docker/node/ps/
		List<String> arguments = new ArrayList<>(6);
		arguments.add("node");
		arguments.add("ps");
		arguments.add("--format");
		arguments.add("json");
		arguments.add("--no-trunc");
		arguments.add(id.getValue());
		CommandResult result = retry(_ -> run(arguments));
		return getNodeParser().listTasksByNode(result);
	}

	@Override
	public NodeId setNodeRole(String id, Role role, Instant deadline)
		throws IOException, InterruptedException, TimeoutException
	{
		return setNodeRole(NodeId.of(id), role, deadline);
	}

	@Override
	public NodeId setNodeRole(NodeId id, Role role, Instant deadline)
		throws IOException, InterruptedException, TimeoutException
	{
		requireThat(id, "id").isNotNull();

		NodeId currentId = getCurrentNodeId();
		if (id.equals(currentId))
		{
			// Although a manager node can technically modify its own role, there is no way of monitoring the
			// change. To ensure observation of the operation, we disallow self-modification and require another
			// manager to perform it.
			throw new IllegalArgumentException("A node cannot modify its own role");
		}

		// https://docs.docker.com/reference/cli/docker/node/update/
		List<String> arguments = List.of("node", "update", "--role=" + role.name().toLowerCase(Locale.ROOT),
			id.getValue());
		CommandResult result = retry(_ -> run(arguments));
		NodeId newId = getNodeParser().setRole(result);
		retry(deadline2 ->
		{
			Node node = getNode(newId);
			while (node.getRole() != role)
			{
				if (!sleepBeforeRetry(deadline2))
					throw new TimeoutException();
				node = getNode(newId);
			}
			return null;
		}, deadline);
		return newId;
	}

	@Override
	public NodeId drainNode(String id) throws IOException, InterruptedException
	{
		return drainNode(NodeId.of(id));
	}

	@Override
	public NodeId drainNode(NodeId id) throws IOException, InterruptedException
	{
		requireThat(id, "id").isNotNull();

		// https://docs.docker.com/reference/cli/docker/node/update/
		List<String> arguments = List.of("node", "update", "--availability=drain", id.getValue());
		CommandResult result = retry(_ -> run(arguments));
		return getNodeParser().setRole(result);
	}

	@Override
	public NodeRemover removeNode(String id)
	{
		return removeNode(NodeId.of(id));
	}

	@Override
	public NodeRemover removeNode(NodeId id)
	{
		return new DefaultNodeRemover(this, id);
	}

	@Override
	public List<Service> getServices() throws IOException, InterruptedException
	{
		return getServices(_ -> true);
	}

	@Override
	public List<Service> getServices(Predicate<ServiceElement> predicate)
		throws IOException, InterruptedException
	{
		// https://docs.docker.com/reference/cli/docker/service/ls/
		List<String> arguments = List.of("service", "ls", "--format", "json", "--no-trunc");
		CommandResult result = retry(_ -> run(arguments));
		List<Service> services = new ArrayList<>();
		for (ServiceElement match : getServiceParser().listServices(result).stream().filter(predicate).toList())
			services.add(getService(match.id()));
		return services;
	}

	@Override
	public Service getService(String id) throws IOException, InterruptedException
	{
		return getService(ServiceId.of(id));
	}

	@Override
	public Service getService(ServiceId id) throws IOException, InterruptedException
	{
		// https://docs.docker.com/reference/cli/docker/service/inspect/
		List<String> arguments = List.of("service", "inspect", "--format", "json", id.getValue());
		CommandResult result = retry(_ -> run(arguments));
		return getServiceParser().getService(result);
	}

	@Override
	public ServiceCreator createService(String imageId)
	{
		return createService(ImageId.of(imageId));
	}

	@Override
	public ServiceCreator createService(ImageId imageId)
	{
		return new DefaultServiceCreator(this, imageId);
	}

	@Override
	public List<Task> getTasksByService(String id) throws IOException, InterruptedException
	{
		return getTasksByService(ServiceId.of(id));
	}

	@Override
	public List<Task> getTasksByService(ServiceId id) throws IOException, InterruptedException
	{
		requireThat(id, "id").isNotNull();

		// https://docs.docker.com/reference/cli/docker/service/ps/
		List<String> arguments = new ArrayList<>(5);
		arguments.add("service");
		arguments.add("ps");
		arguments.add("--format");
		arguments.add("json");
		arguments.add(id.getValue());
		CommandResult result = retry(_ -> run(arguments));
		return getNodeParser().listTasksByService(result);
	}

	@Override
	public Task getTask(String id) throws IOException, InterruptedException
	{
		return getTask(TaskId.of(id));
	}

	@Override
	public Task getTask(TaskId id) throws IOException, InterruptedException
	{
		requireThat(id, "id").isNotNull();

		// https://docs.docker.com/reference/cli/docker/inspect/
		List<String> arguments = new ArrayList<>(4);
		arguments.add("inspect");
		arguments.add("--type");
		arguments.add("task");
		arguments.add(id.getValue());
		CommandResult result = retry(_ -> run(arguments));
		return getNodeParser().getTaskState(result);
	}

	@Override
	public List<Object> getAll(Predicate<? super Class<?>> typeFilter, Predicate<Object> resourceFilter)
		throws IOException, InterruptedException
	{
		Set<Class<?>> types = Set.of(Config.class, Container.class, Context.class, DockerImage.class,
			Network.class, Node.class, Service.class, Task.class);
		types = types.stream().filter(typeFilter).collect(Collectors.toSet());
		if (types.isEmpty())
			return List.of();
		try (ShutdownOnFailure scope = new ShutdownOnFailure("docker.getAll()", THREAD_FACTORY))
		{
			Supplier<List<Config>> configs;
			if (types.contains(Config.class))
				configs = scope.fork(() -> getConfigs(resourceFilter::test));
			else
				configs = List::of;

			Supplier<List<Container>> containers;
			if (types.contains(Container.class))
				containers = scope.fork(() -> getContainers(resourceFilter::test));
			else
				containers = List::of;

			Supplier<List<Context>> contexts;
			if (types.contains(Context.class))
				contexts = scope.fork(() -> getContexts(resourceFilter::test));
			else
				contexts = List::of;

			Supplier<List<DockerImage>> images;
			if (types.contains(DockerImage.class))
				images = scope.fork(() -> getImages(resourceFilter::test));
			else
				images = List::of;

			Supplier<List<Network>> networks;
			if (types.contains(Network.class))
				networks = scope.fork(() -> getNetworks(resourceFilter::test));
			else
				networks = List::of;

			Supplier<List<Node>> nodes;
			if (types.contains(Node.class))
				nodes = scope.fork(() -> getNodes(resourceFilter::test));
			else
				nodes = List::of;

			Supplier<List<Service>> services;
			if (types.contains(Service.class))
				services = scope.fork(() -> getServices(resourceFilter::test));
			else
				services = List::of;

			try
			{
				scope.join().throwIfFailed();
			}
			catch (ExecutionException e)
			{
				if (e.getCause() instanceof IOException ioe)
					throw ioe;
				throw WrappedCheckedException.wrap(e);
			}

			List<Service> servicesAsList = services.get();
			List<Task> tasks = new ArrayList<>();
			if (types.contains(Task.class))
				for (Service service : servicesAsList)
					tasks.addAll(getTasksByService(service.getId()));

			return Lists.combine(configs.get(), containers.get(), contexts.get(), images.get(), networks.get(),
				nodes.get(), servicesAsList, tasks);
		}
	}

	@Override
	public void close()
	{
	}
}