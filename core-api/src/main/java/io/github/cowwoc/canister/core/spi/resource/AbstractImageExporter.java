package io.github.cowwoc.canister.core.spi.resource;

import io.github.cowwoc.canister.core.resource.ImageBuilder.CompressionType;

/**
 * Code common to all exporters that output an image.
 */
public abstract sealed class AbstractImageExporter extends AbstractExporter
	permits DockerImageExporter, OciImageExporter, RegistryExporter
{
	protected final String name;
	protected final CompressionType compressionType;
	protected final int compressionLevel;

	/**
	 * Creates a AbstractImageExporter.
	 *
	 * @param name             the name of the image
	 * @param compressionType  the type of compression to use
	 * @param compressionLevel the compression level to use
	 */
	protected AbstractImageExporter(String name, CompressionType compressionType, int compressionLevel)
	{
		this.name = name;
		this.compressionType = compressionType;
		this.compressionLevel = compressionLevel;
	}
}