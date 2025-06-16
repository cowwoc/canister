package io.github.cowwoc.canister.core.spi.resource;

import io.github.cowwoc.canister.core.resource.ImageBuilder;
import io.github.cowwoc.canister.core.resource.ImageBuilder.CompressionType;
import io.github.cowwoc.canister.core.resource.ImageBuilder.Exporter;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Builds an exporter that outputs images to disk using the OCI container format.
 */
public final class DefaultOciImageExporterBuilder extends AbstractImageExporterBuilder
	implements ImageBuilder.OciImageExporterBuilder
{
	private final String path;
	private boolean directory;
	private String context = "";

	/**
	 * Creates a OciImageExporterBuilder.
	 * <p>
	 * For multi-platform builds, a separate subdirectory will be created for each platform.
	 * <p>
	 * For example, the directory structure might look like:
	 * <pre>{@code
	 * /
	 * ├── linux_amd64/
	 * └── linux_arm64/
	 * }</pre>
	 *
	 * @param path the output location, which is either a TAR archive or a directory depending on whether
	 *             {@link #directory() directory()} is invoked
	 */
	public DefaultOciImageExporterBuilder(String path)
	{
		requireThat(path, "path").doesNotContainWhitespace().isNotEmpty();
		this.path = path;
	}

	@Override
	public ImageBuilder.OciImageExporterBuilder name(String name)
	{
		return (ImageBuilder.OciImageExporterBuilder) super.name(name);
	}

	@Override
	public ImageBuilder.OciImageExporterBuilder compressionType(CompressionType type)
	{
		return (ImageBuilder.OciImageExporterBuilder) super.compressionType(type);
	}

	@Override
	public ImageBuilder.OciImageExporterBuilder compressionLevel(int compressionLevel)
	{
		return (ImageBuilder.OciImageExporterBuilder) super.compressionLevel(compressionLevel);
	}

	@Override
	public ImageBuilder.OciImageExporterBuilder directory()
	{
		this.directory = true;
		return this;
	}

	@Override
	public ImageBuilder.OciImageExporterBuilder context(String context)
	{
		requireThat(context, "context").doesNotContainWhitespace().isNotEmpty();
		this.context = context;
		return this;
	}

	@Override
	public Exporter build()
	{
		return new OciImageExporter(path, directory, name, compressionType, compressionLevel, context);
	}
}