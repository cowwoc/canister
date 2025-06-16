package io.github.cowwoc.canister.core.spi.resource;

import io.github.cowwoc.canister.core.resource.ImageBuilder.CompressionType;
import io.github.cowwoc.canister.core.resource.ImageBuilder.Exporter;

import java.util.StringJoiner;

/**
 * Builds an exporter that outputs images to disk using the OCI container format.
 */
public final class OciImageExporter extends AbstractImageExporter
	implements Exporter
{
	private final String path;
	private final boolean directory;
	private final String context;

	/**
	 * Creates a OciImageExporter.
	 *
	 * @param path             the output path
	 * @param directory        specifies that the image files should be written to a directory. By default, the
	 *                         image is packaged as a TAR archive, with {@code path} representing the archive’s
	 *                         location. When this method is used, {@code path} is treated as a directory, and
	 *                         image files are written directly into it.
	 * @param name             the name of the image
	 * @param compressionType  the type of compression to use
	 * @param compressionLevel the compression level to use
	 * @param context          the Docker context into which the built image should be imported. If omitted, the
	 *                         image is imported into the same context in which the build was executed.
	 */
	public OciImageExporter(String path, boolean directory, String name, CompressionType compressionType,
		int compressionLevel, String context)
	{
		super(name, compressionType, compressionLevel);
		assert path != null;
		assert context != null;
		this.path = path;
		this.directory = directory;
		this.context = context;
	}

	@Override
	public String getType()
	{
		return "oci";
	}

	@Override
	public boolean loadsIntoImageStore()
	{
		return false;
	}

	@Override
	public String toCommandLine()
	{
		StringJoiner joiner = new StringJoiner(",");
		joiner.add("type=" + getType());
		if (path != null)
			joiner.add("dest=" + path);
		if (!name.isEmpty())
			joiner.add("name=" + name);
		if (directory)
			joiner.add("tar=false");
		if (compressionType != CompressionType.GZIP)
			joiner.add("compression=" + compressionType.toCommandLine());
		if (compressionLevel != -1)
			joiner.add("compression-level=" + compressionLevel);
		if (!context.isEmpty())
			joiner.add("context=" + context);
		return joiner.toString();
	}
}