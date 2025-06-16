package io.github.cowwoc.canister.docker.main.resource;

import io.github.cowwoc.canister.core.resource.CommandResult;
import io.github.cowwoc.canister.core.spi.util.ToStringBuilder;
import io.github.cowwoc.canister.docker.id.ContextId;
import io.github.cowwoc.canister.docker.main.client.InternalDockerClient;
import io.github.cowwoc.canister.docker.resource.ContextRemover;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Default implementation of {@code ContextRemover}.
 */
public final class DefaultContextRemover implements ContextRemover
{
	private final InternalDockerClient client;
	private final ContextId id;
	private boolean force;

	/**
	 * Creates a context remover.
	 *
	 * @param client the client configuration
	 * @param id     the ID of the context
	 * @throws NullPointerException if {@code id} is null
	 */
	public DefaultContextRemover(InternalDockerClient client, ContextId id)
	{
		assert client != null;
		requireThat(id, "id").isNotNull();
		this.client = client;
		this.id = id;
	}

	@Override
	public ContextRemover force()
	{
		this.force = true;
		return this;
	}

	@Override
	public void remove() throws IOException, InterruptedException
	{
		// https://docs.docker.com/reference/cli/docker/context/rm/
		List<String> arguments = new ArrayList<>(4);
		arguments.add("context");
		arguments.add("rm");
		if (force)
			arguments.add("--force");
		arguments.add(id.getValue());
		CommandResult result = client.retry(_ -> client.run(arguments));
		client.getContextParser().remove(result);
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(DefaultContextRemover.class).
			add("force", force).
			toString();
	}
}