package io.github.cowwoc.canister.core.spi.resource;

import io.github.cowwoc.canister.core.resource.ImageBuilder;
import io.github.cowwoc.canister.core.resource.ImageBuilder.CompressionType;
import io.github.cowwoc.canister.core.resource.ImageBuilder.Exporter;

/**
 * Builds an exporter that outputs images to a registry.
 */
public final class DefaultRegistryExporterBuilder extends AbstractImageExporterBuilder
	implements ImageBuilder.RegistryExporterBuilder
{
	/**
	 * Creates an DefaultRegistryExporterBuilder.
	 */
	public DefaultRegistryExporterBuilder()
	{
	}

	@Override
	public ImageBuilder.RegistryExporterBuilder name(String name)
	{
		return (ImageBuilder.RegistryExporterBuilder) super.name(name);
	}

	@Override
	public ImageBuilder.RegistryExporterBuilder compressionType(CompressionType type)
	{
		return (ImageBuilder.RegistryExporterBuilder) super.compressionType(type);
	}

	@Override
	public ImageBuilder.RegistryExporterBuilder compressionLevel(int compressionLevel)
	{
		return (ImageBuilder.RegistryExporterBuilder) super.compressionLevel(compressionLevel);
	}

	@Override
	public Exporter build()
	{
		return new RegistryExporter(name, compressionType, compressionLevel);
	}
}