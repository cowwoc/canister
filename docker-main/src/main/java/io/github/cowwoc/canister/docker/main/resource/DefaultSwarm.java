package io.github.cowwoc.canister.docker.main.resource;

import io.github.cowwoc.canister.core.spi.util.ToStringBuilder;
import io.github.cowwoc.canister.docker.main.client.InternalDockerClient;
import io.github.cowwoc.canister.docker.resource.DockerImage;
import io.github.cowwoc.canister.docker.resource.JoinToken;
import io.github.cowwoc.canister.docker.resource.Swarm;
import io.github.cowwoc.canister.docker.resource.SwarmLeaver;

import java.io.IOException;

public final class DefaultSwarm implements Swarm
{
	private final InternalDockerClient client;

	/**
	 * Creates a reference to a Swarm.
	 *
	 * @param client the client configuration
	 */
	public DefaultSwarm(InternalDockerClient client)
	{
		assert client != null;
		this.client = client;
	}

	@Override
	public SwarmLeaver leave()
	{
		return client.leaveSwarm();
	}

	@Override
	public JoinToken getManagerJoinToken() throws IOException, InterruptedException
	{
		return client.getManagerJoinToken();
	}

	@Override
	public JoinToken getWorkerJoinToken() throws IOException, InterruptedException
	{
		return client.getWorkerJoinToken();
	}

	@Override
	public int hashCode()
	{
		return 0;
	}

	@Override
	public boolean equals(Object o)
	{
		return o instanceof Swarm;
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(DockerImage.class).
			toString();
	}
}