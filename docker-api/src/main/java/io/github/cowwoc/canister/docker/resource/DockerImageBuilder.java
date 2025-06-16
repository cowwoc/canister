package io.github.cowwoc.canister.docker.resource;

import io.github.cowwoc.canister.core.id.BuilderId;
import io.github.cowwoc.canister.core.resource.BuildListener;
import io.github.cowwoc.canister.core.resource.ImageBuilder;

import java.nio.file.Path;

/**
 * Represents an operation that builds an image using Docker.
 */
public interface DockerImageBuilder extends ImageBuilder
{
	@Override
	DockerImageBuilder dockerfile(Path dockerfile);

	@Override
	DockerImageBuilder platform(String platform);

	@Override
	DockerImageBuilder reference(String reference);

	@Override
	DockerImageBuilder cacheFrom(String source);

	@Override
	DockerImageBuilder export(Exporter exporter);

	@Override
	DockerImageBuilder builder(BuilderId builder);

	@Override
	DockerImageBuilder listener(BuildListener listener);
}