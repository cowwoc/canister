package io.github.cowwoc.canister.core.spi.resource;

import io.github.cowwoc.canister.core.resource.ImageBuilder.CompressionType;
import io.github.cowwoc.canister.core.resource.ImageBuilder.Exporter;

import java.util.StringJoiner;

/**
 * Builds an exporter that outputs images to a registry.
 */
public final class RegistryExporter extends AbstractImageExporter
	implements Exporter
{
	/**
	 * Creates a RegistryExporter.
	 *
	 * @param name             the name of the image
	 * @param compressionType  the type of compression to use
	 * @param compressionLevel the compression level to use
	 */
	public RegistryExporter(String name, CompressionType compressionType, int compressionLevel)
	{
		super(name, compressionType, compressionLevel);
	}

	@Override
	public String getType()
	{
		return "registry";
	}

	@Override
	public boolean loadsIntoImageStore()
	{
		return true;
	}

	@Override
	public String toCommandLine()
	{
		StringJoiner joiner = new StringJoiner(",");
		joiner.add("type=registry");
		if (!name.isEmpty())
			joiner.add("name=" + name);
		if (compressionType != CompressionType.GZIP)
			joiner.add("compression=" + compressionType.toCommandLine());
		if (compressionLevel != -1)
			joiner.add("compression-level=" + compressionLevel);
		return joiner.toString();
	}
}