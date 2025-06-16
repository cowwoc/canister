package io.github.cowwoc.canister.core.spi.resource;

import io.github.cowwoc.canister.core.resource.ImageBuilder.CompressionType;
import io.github.cowwoc.canister.core.resource.ImageBuilder.ImageExporterBuilder;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Builds an exporter that outputs images.
 */
public abstract sealed class AbstractImageExporterBuilder implements ImageExporterBuilder
	permits DefaultContentsExporterBuilder, DefaultDockerImageExporterBuilder, DefaultOciImageExporterBuilder, DefaultRegistryExporterBuilder
{
	/**
	 * The name of the image.
	 */
	protected String name = "";
	/**
	 * The type of compression to use.
	 */
	protected CompressionType compressionType = CompressionType.GZIP;
	/**
	 * The compression level to use.
	 */
	protected int compressionLevel = -1;

	@Override
	public ImageExporterBuilder name(String name)
	{
		requireThat(name, "name").doesNotContainWhitespace().isNotEmpty();
		this.name = name;
		return this;
	}

	@Override
	public ImageExporterBuilder compressionType(CompressionType type)
	{
		requireThat(type, "type").isNotNull();
		this.compressionType = type;
		return this;
	}

	@Override
	public ImageExporterBuilder compressionLevel(int compressionLevel)
	{
		switch (compressionType)
		{
			case UNCOMPRESSED ->
			{
			}
			case GZIP, ESTARGZ -> requireThat(compressionLevel, "compressionLevel").isBetween(0, 9);
			case ZSTD -> requireThat(compressionLevel, "compressionLevel").isBetween(0, 22);
		}
		this.compressionLevel = compressionLevel;
		return this;
	}
}
