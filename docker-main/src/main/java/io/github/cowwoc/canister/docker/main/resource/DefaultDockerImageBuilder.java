package io.github.cowwoc.canister.docker.main.resource;

import io.github.cowwoc.canister.buildx.internal.resource.DefaultImageBuilder;
import io.github.cowwoc.canister.core.id.BuilderId;
import io.github.cowwoc.canister.core.resource.BuildListener;
import io.github.cowwoc.canister.docker.main.client.InternalDockerClient;
import io.github.cowwoc.canister.docker.resource.DockerImageBuilder;

import java.nio.file.Path;

/**
 * Represents an operation that builds an image.
 */
public final class DefaultDockerImageBuilder extends DefaultImageBuilder
	implements DockerImageBuilder
{
	/**
	 * Creates a DefaultDockerImageBuilder.
	 *
	 * @param client the client configuration
	 */
	public DefaultDockerImageBuilder(InternalDockerClient client)
	{
		super(client);
	}

	@Override
	public DockerImageBuilder dockerfile(Path dockerfile)
	{
		return (DockerImageBuilder) super.dockerfile(dockerfile);
	}

	@Override
	public DockerImageBuilder platform(String platform)
	{
		return (DockerImageBuilder) super.platform(platform);
	}

	@Override
	public DockerImageBuilder reference(String reference)
	{
		return (DockerImageBuilder) super.reference(reference);
	}

	@Override
	public DockerImageBuilder cacheFrom(String source)
	{
		return (DockerImageBuilder) super.cacheFrom(source);
	}

	@Override
	public DockerImageBuilder export(Exporter exporter)
	{
		return (DockerImageBuilder) super.export(exporter);
	}

	@Override
	public DockerImageBuilder builder(BuilderId builder)
	{
		return (DockerImageBuilder) super.builder(builder);
	}

	@Override
	public DockerImageBuilder listener(BuildListener listener)
	{
		return (DockerImageBuilder) super.listener(listener);
	}
}