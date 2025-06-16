package io.github.cowwoc.canister.docker.main.resource;

import io.github.cowwoc.canister.core.resource.CommandResult;
import io.github.cowwoc.canister.core.spi.util.ToStringBuilder;
import io.github.cowwoc.canister.docker.id.ContainerId;
import io.github.cowwoc.canister.docker.main.client.InternalDockerClient;
import io.github.cowwoc.canister.docker.resource.ContainerRemover;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

public final class DefaultContainerRemover implements ContainerRemover
{
	private final InternalDockerClient client;
	private final ContainerId id;
	private boolean force;
	private boolean volumes;

	/**
	 * Creates a container remover.
	 *
	 * @param id     the container's ID or name
	 * @param client the client configuration
	 * @throws NullPointerException if {@code id} is null
	 */
	public DefaultContainerRemover(InternalDockerClient client, ContainerId id)
	{
		assert client != null;
		requireThat(id, "id").isNotNull();
		this.client = client;
		this.id = id;
	}

	@Override
	public ContainerRemover kill()
	{
		this.force = true;
		return this;
	}

	@Override
	public ContainerRemover removeAnonymousVolumes()
	{
		this.volumes = true;
		return this;
	}

	@Override
	public void apply() throws IOException, InterruptedException
	{
		// https://docs.docker.com/reference/cli/docker/container/rm/
		List<String> arguments = new ArrayList<>(5);
		arguments.add("container");
		arguments.add("rm");
		if (force)
			arguments.add("--force");
		if (volumes)
			arguments.add("--volumes");
		arguments.add(id.getValue());
		CommandResult result = client.retry(_ -> client.run(arguments));
		client.getContainerParser().remove(result);
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(ContainerRemover.class).
			add("force", force).
			add("volumes", volumes).
			toString();
	}
}