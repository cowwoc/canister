package io.github.cowwoc.canister.docker.main.resource;

import io.github.cowwoc.canister.core.spi.util.ToStringBuilder;
import io.github.cowwoc.canister.docker.id.ServiceId;
import io.github.cowwoc.canister.docker.main.client.InternalDockerClient;
import io.github.cowwoc.canister.docker.resource.DockerImage;
import io.github.cowwoc.canister.docker.resource.Service;
import io.github.cowwoc.canister.docker.resource.Task;

import java.io.IOException;
import java.util.List;

public final class DefaultService implements Service
{
	private final InternalDockerClient client;
	private final ServiceId id;
	private final String name;

	/**
	 * Creates a reference to a service.
	 *
	 * @param client the client configuration
	 * @param id     the ID of the service
	 * @param name   the name of the service
	 */
	public DefaultService(InternalDockerClient client, ServiceId id, String name)
	{
		assert client != null;
		assert id != null;
		assert name != null;

		this.client = client;
		this.id = id;
		this.name = name;
	}

	@Override
	public ServiceId getId()
	{
		return id;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public List<Task> listTasks() throws IOException, InterruptedException
	{
		return client.getTasksByService(id);
	}

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}

	@Override
	public boolean equals(Object o)
	{
		return o instanceof Service other && other.getId().equals(id);
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(DockerImage.class).
			add("id", id).
			add("name", name).
			toString();
	}
}