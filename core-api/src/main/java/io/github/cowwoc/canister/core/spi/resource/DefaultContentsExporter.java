package io.github.cowwoc.canister.core.spi.resource;

import io.github.cowwoc.canister.core.resource.ImageBuilder.Exporter;

/**
 * Exports the contents of an image.
 */
public final class DefaultContentsExporter extends AbstractExporter
	implements Exporter
{
	private final String path;
	private final boolean directory;

	/**
	 * Creates a ContentsExporter.
	 *
	 * @param path      the output location
	 * @param directory {@code true} if {@code path} refers to a directory; otherwise, it refers to a TAR
	 *                  archive
	 */
	public DefaultContentsExporter(String path, boolean directory)
	{
		assert path != null;
		this.path = path;
		this.directory = directory;
	}

	@Override
	public String getType()
	{
		if (directory)
			return "local";
		return "tar";
	}

	@Override
	public boolean loadsIntoImageStore()
	{
		return false;
	}

	@Override
	public String toCommandLine()
	{
		if (directory)
			return "type=local,dest=" + path;
		return "type=tar,dest=" + path;
	}
}