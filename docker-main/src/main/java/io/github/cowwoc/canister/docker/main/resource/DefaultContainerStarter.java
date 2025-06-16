package io.github.cowwoc.canister.docker.main.resource;

import io.github.cowwoc.canister.core.resource.CommandResult;
import io.github.cowwoc.canister.core.spi.util.ToStringBuilder;
import io.github.cowwoc.canister.docker.id.ContainerId;
import io.github.cowwoc.canister.docker.main.client.InternalDockerClient;
import io.github.cowwoc.canister.docker.resource.Container;
import io.github.cowwoc.canister.docker.resource.ContainerStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

public final class DefaultContainerStarter implements ContainerStarter
{
	private final InternalDockerClient client;
	private final ContainerId id;
	private final Logger log = LoggerFactory.getLogger(DefaultContainerStarter.class);

	/**
	 * Creates a container starter.
	 *
	 * @param client the client configuration
	 * @param id     the container's ID or name
	 * @throws NullPointerException if {@code id} is null
	 */
	public DefaultContainerStarter(InternalDockerClient client, ContainerId id)
	{
		assert client != null;
		requireThat(id, "id").isNotNull();
		this.client = client;
		this.id = id;
	}

	@Override
	public Container apply() throws IOException, InterruptedException
	{
		// https://docs.docker.com/reference/cli/docker/container/start/
		List<String> arguments = List.of("container", "start", id.getValue());
		CommandResult result = client.retry(_ -> client.run(arguments));
		client.getContainerParser().start(result);
		return client.getContainer(id);
	}

	@Override
	public ContainerStreams applyAndAttachStreams(boolean attachInput,
		boolean attachOutput) throws IOException
	{
		// https://docs.docker.com/reference/cli/docker/container/start/
		List<String> arguments = new ArrayList<>(5);
		arguments.add("container");
		arguments.add("start");
		if (attachOutput)
			arguments.add("--attach");
		if (attachInput)
			arguments.add("--interactive");
		arguments.add(id.getValue());

		ProcessBuilder processBuilder = client.getProcessBuilder(arguments);
		log.debug("Running: {}", processBuilder.command());
		Process process = processBuilder.start();
		return new DefaultContainerStreams(process);
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(DefaultContainerStarter.class).
			add("id", id).
			toString();
	}

	public static final class DefaultContainerStreams implements ContainerStreams
	{
		private final Process process;
		private final OutputStream stdin;
		private final InputStream stdout;
		private final InputStream stderr;

		/**
		 * Creates a DefaultContainerStreams.
		 *
		 * @param process the docker process
		 */
		public DefaultContainerStreams(Process process)
		{
			this.process = process;
			this.stdin = process.getOutputStream();
			this.stdout = process.getInputStream();
			this.stderr = process.getErrorStream();
		}

		@Override
		public OutputStream getStdin()
		{
			return stdin;
		}

		@Override
		public InputStream getStdout()
		{
			return stdout;
		}

		@Override
		public InputStream getStderr()
		{
			return stderr;
		}

		@Override
		public int waitFor() throws InterruptedException
		{
			return process.waitFor();
		}

		@Override
		@SuppressWarnings("EmptyTryBlock")
		public void close() throws IOException
		{
			try (stdin; stdout; stderr)
			{
			}
		}
	}
}