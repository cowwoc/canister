package io.github.cowwoc.canister.buildx.internal.resource;

import io.github.cowwoc.canister.buildx.internal.client.InternalBuildXClient;
import io.github.cowwoc.canister.core.id.BuilderId;
import io.github.cowwoc.canister.core.resource.BuilderRemover;
import io.github.cowwoc.canister.core.resource.CommandResult;
import io.github.cowwoc.canister.core.spi.util.ToStringBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

public final class DefaultBuilderRemover implements BuilderRemover
{
	private final InternalBuildXClient client;
	private final BuilderId id;
	private boolean keepState;

	/**
	 * Creates a builder remover.
	 *
	 * @param client the client configuration
	 * @param id     the ID of the builder
	 */
	public DefaultBuilderRemover(InternalBuildXClient client, BuilderId id)
	{
		assert client != null;
		requireThat(id, "id").isNotNull();
		this.client = client;
		this.id = id;
	}

	@Override
	public BuilderRemover keepState()
	{
		this.keepState = true;
		return this;
	}

	@Override
	public void apply() throws IOException, InterruptedException
	{
		// https://docs.docker.com/reference/cli/docker/buildx/rm/
		List<String> arguments = new ArrayList<>(4);
		arguments.add("buildx");
		arguments.add("rm");
		arguments.add(id.getValue());
		if (keepState)
			arguments.add("--keep-state");
		client.retry(_ ->
		{
			CommandResult result = client.run(arguments);
			client.getBuildXParser().remove(result);
			return null;
		});
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(DefaultBuilderRemover.class).
			add("id", id).
			add("keepState", keepState).
			toString();
	}
}