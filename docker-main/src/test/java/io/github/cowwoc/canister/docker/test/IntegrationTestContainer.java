package io.github.cowwoc.canister.docker.test;

import io.github.cowwoc.canister.core.id.BuilderId;
import io.github.cowwoc.canister.core.resource.Builder.Node.Status;
import io.github.cowwoc.canister.docker.api.client.Docker;
import io.github.cowwoc.canister.docker.api.client.DockerClient;
import io.github.cowwoc.canister.docker.main.client.InternalDockerClient;
import io.github.cowwoc.canister.docker.resource.Container;
import io.github.cowwoc.canister.docker.resource.ContainerCreator.PortBindingBuilder;
import io.github.cowwoc.canister.docker.resource.Context;
import io.github.cowwoc.canister.docker.resource.ContextEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.that;

/**
 * Represents a Docker container for running an integration test.
 */
public class IntegrationTestContainer
{
	/**
	 * Default port number for Docker Engine when using TLS.
	 */
	private static final int DOCKER_TLS_PORT = 2376;
	private static final String DOCKER_IN_DOCKER = "docker:dind";
	private final InternalDockerClient client;
	private final String name;
	private final Logger log = LoggerFactory.getLogger(IntegrationTestContainer.class);

	/**
	 * Creates an integration test.
	 *
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 * @throws TimeoutException     if a timeout occurs before the container is ready
	 */
	public IntegrationTestContainer() throws IOException, InterruptedException, TimeoutException
	{
		// Ensure a consistent stack-trace depth across all constructors for getCallerName() to work properly
		this("", true);
	}

	/**
	 * Creates an integration test.
	 *
	 * @param suffix the suffix to append to the container name
	 * @throws NullPointerException     if {@code suffix} is null
	 * @throws IllegalArgumentException if {@code suffix} contains whitespace
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 * @throws TimeoutException         if a timeout occurs before the container is ready
	 */
	public IntegrationTestContainer(String suffix) throws IOException, InterruptedException, TimeoutException
	{
		this(suffix, true);
	}

	/**
	 * Creates an integration test.
	 *
	 * @param suffix  the suffix to append to the container name
	 * @param ignored a value used to differentiate between constructors
	 * @throws NullPointerException     if {@code suffix} is null
	 * @throws IllegalArgumentException if {@code suffix} contains whitespace
	 * @throws IOException              if an I/O error if the default Docker client cannot be found, or an
	 *                                  error occurs while reading its file attributes
	 * @throws InterruptedException     if the thread is interrupted before the operation is complete
	 */
	private IntegrationTestContainer(String suffix,
		boolean ignored) throws IOException, InterruptedException, TimeoutException
	{
		assert that(suffix, "suffix").doesNotContainWhitespace().elseThrow();
		String name = getCallerName();
		if (!suffix.isEmpty())
			name += "." + suffix;
		this.name = name;
		this.client = (InternalDockerClient) Docker.fromPath();
		Path tlsCertificates = Path.of("certs");
		Path caCertificate = tlsCertificates.resolve("ca/cert.pem");
		Path clientCertificate = tlsCertificates.resolve("client/cert.pem");
		Path clientPrivateKey = tlsCertificates.resolve("client/key.pem");

		log.debug("Container.remove()");
		// Remove resources left over from terminated debug sessions
		client.removeContainer(name).kill().removeAnonymousVolumes().apply();
		log.debug("Context.remove()");
		client.removeContext(name).remove();

		log.debug("Image.pull()");
		if (client.getImage(DOCKER_IN_DOCKER) == null)
		{
			// Improves performance and reduces the chance that we'll surpass DockerHub's rate limits
			client.pullImage(DOCKER_IN_DOCKER).apply();
		}
		log.debug("Container.create()");
		// See https://hub.docker.com/_/docker
		Container container = client.createContainer(DOCKER_IN_DOCKER).
			name(name).
			privileged().
			environmentVariable("DOCKER_TLS_CERTDIR", "/certs").
			bindPort(new PortBindingBuilder(DOCKER_TLS_PORT).hostAddress(InetAddress.getLoopbackAddress()).build()).
			bindPath(tlsCertificates, "/certs").
			// Enable multi-platform builds inside the container by using the containerd image store
			// https://github.com/docker-library/docker/issues/477#issuecomment-2500964552
				arguments("--feature", "containerd-snapshotter=true").
			apply();
		int hostPort;
		do
		{
			log.debug("Container.start()");
			container.start().apply();
			log.debug("client.getContainer()");
			container = container.reload();
			hostPort = getHostPort(container);
		}
		while (hostPort == -1);
		ContextEndpoint endpoint = ContextEndpoint.builder(URI.create("tcp://localhost:" + hostPort)).
			tls(caCertificate, clientCertificate, clientPrivateKey).build();
		log.debug("Container.waitUntilReady()");
		createContext(endpoint);
		log.debug("Container.isReady()");
	}

	private static int getHostPort(Container container) throws IOException, InterruptedException
	{
		for (Container.PortBinding port : container.getNetworkConfiguration().ports())
		{
			if (port.containerPort() == DOCKER_TLS_PORT)
			{
				List<InetSocketAddress> hostAddresses = port.hostAddresses();
				if (hostAddresses.isEmpty())
				{
					// Docker sometimes fails to expose the port. Could be related to this bug:
					// https://github.com/moby/moby/issues/44137
					//
					// Stop the container and try again.
					container.stop().apply();
					return -1;
				}
				return hostAddresses.getFirst().getPort();
			}
		}
		throw new IOException("Expected the container to listen on port " + DOCKER_TLS_PORT + "\n" +
			"Actual: " + container);
	}

	/**
	 * Returns the name of the test.
	 *
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Creates a context and waits until the default image builder on it is ready.
	 *
	 * @param endpoint the configuration of the target Docker Engine
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 * @throws TimeoutException     if a timeout occurs before the container is ready
	 */
	private void createContext(ContextEndpoint endpoint)
		throws InterruptedException, IOException, TimeoutException
	{
		// docker:dind writes client/key.pem last, so we only need to wait for that file to ensure all
		// the certificates are fully generated:
		// https://github.com/docker-library/docker/blob/a89fda523324cef5da11eed61c397347e1435edd/dockerd-entrypoint.sh#L75

		Instant deadline = Instant.now().plus(client.getRetryTimeout());
		Context context = client.retry(_ ->
		{
			Context candidate = client.createContext(name, endpoint).apply();
			client.setClientContext(candidate.getId());
			return candidate;
		}, deadline);
		client.waitUntilBuilderStatus(BuilderId.of(context.getId().getValue()), Status.RUNNING, deadline);
	}

	/**
	 * Returns the name of the class + method that instantiated this object.
	 *
	 * @return the caller's name
	 */
	private static String getCallerName()
	{
		// https://stackoverflow.com/a/52335318/14731
		StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).
			walk(frame -> frame.skip(3).findFirst().
				orElseThrow());
		return caller.getDeclaringClass().getSimpleName() + "." + caller.getMethodName();
	}

	/**
	 * Returns the docker engine inside the container.
	 *
	 * @return the docker engine
	 */
	public DockerClient getClient()
	{
		return client;
	}

	/**
	 * Handles the successful completion of a test by removing the associated container.
	 * <p>
	 * Containers for failed tests are retained for further inspection.
	 *
	 * @throws InterruptedException if the thread is interrupted before the operation completes
	 * @throws IOException          if an I/O error if the default Docker client cannot be found, or an error
	 *                              occurs while reading its file attributes
	 */
	public void onSuccess() throws IOException, InterruptedException
	{
		client.setClientContext("");
		log.debug("Context.remove()");
		client.removeContext(name).force().remove();
		log.debug("Container.remove()");
		client.removeContainer(name).kill().removeAnonymousVolumes().apply();
		log.debug("Container.removed()");
		// Source: https://mail.openjdk.org/pipermail/coin-dev/2011-March/003162.html
		// AutoCloseable.close() may not throw InterruptedException, as try-with-resources could suppress
		// it, causing higher-level catch statements to miss the interruption. The recommended approach is
		// to catch the exception, restore the thread's interrupt status, and complete close() as quickly
		// and safely as possible. Restoring the interrupt flag ensures that subsequent blocking calls
		// respond immediately to interruption.
		//
		// Thread.currentThread().interrupt();
	}
}