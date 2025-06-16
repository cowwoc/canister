package io.github.cowwoc.canister.core.spi.resource;

import io.github.cowwoc.canister.core.resource.ImageBuilder;
import io.github.cowwoc.canister.core.resource.ImageBuilder.CompressionType;
import io.github.cowwoc.canister.core.resource.ImageBuilder.DockerImageExporterBuilder;
import io.github.cowwoc.canister.core.resource.ImageBuilder.Exporter;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Builds an exporter that outputs images using the Docker container format.
 */
public final class DefaultDockerImageExporterBuilder extends AbstractImageExporterBuilder
	implements DockerImageExporterBuilder
{
	private String path = "";
	private String context = "";

	/**
	 * Creates a DefaultDockerImageExporterBuilder.
	 */
	public DefaultDockerImageExporterBuilder()
	{
	}

	@Override
	public ImageBuilder.DockerImageExporterBuilder name(String name)
	{
		return (ImageBuilder.DockerImageExporterBuilder) super.name(name);
	}

	@Override
	public ImageBuilder.DockerImageExporterBuilder compressionType(CompressionType type)
	{
		return (ImageBuilder.DockerImageExporterBuilder) super.compressionType(type);
	}

	@Override
	public ImageBuilder.DockerImageExporterBuilder compressionLevel(int compressionLevel)
	{
		return (ImageBuilder.DockerImageExporterBuilder) super.compressionLevel(compressionLevel);
	}

	@Override
	public ImageBuilder.DockerImageExporterBuilder path(String path)
	{
		requireThat(path, "path").isStripped().isNotEmpty();
		this.path = path;
		return this;
	}

	@Override
	public ImageBuilder.DockerImageExporterBuilder context(String context)
	{
		requireThat(context, "context").doesNotContainWhitespace().isNotEmpty();
		this.context = context;
		return this;
	}

	@Override
	public Exporter build()
	{
		return new DockerImageExporter(path, name, compressionType, compressionLevel, context);
	}
}
