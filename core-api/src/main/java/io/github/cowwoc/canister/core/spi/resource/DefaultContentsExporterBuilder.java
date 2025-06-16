package io.github.cowwoc.canister.core.spi.resource;

import io.github.cowwoc.canister.core.resource.ImageBuilder;
import io.github.cowwoc.canister.core.resource.ImageBuilder.ContentsExporterBuilder;
import io.github.cowwoc.canister.core.resource.ImageBuilder.Exporter;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Builds an exporter that outputs the contents of images to disk.
 */
public final class DefaultContentsExporterBuilder extends AbstractImageExporterBuilder
	implements ContentsExporterBuilder
{
	private final String path;
	private boolean directory;

	/**
	 * Creates a DefaultContentsExporterBuilder.
	 *
	 * @param path the output location, which is either a TAR archive or a directory depending on whether
	 *             {@link #directory()} is invoked
	 * @throws NullPointerException     if {@code path} is null
	 * @throws IllegalArgumentException if {@code path} contains leading or trailing whitespace or is empty
	 */
	public DefaultContentsExporterBuilder(String path)
	{
		requireThat(path, "path").isStripped().isNotEmpty();
		this.path = path;
	}

	@Override
	public ImageBuilder.ContentsExporterBuilder directory()
	{
		this.directory = true;
		return this;
	}

	@Override
	public Exporter build()
	{
		return new DefaultContentsExporter(path, directory);
	}
}