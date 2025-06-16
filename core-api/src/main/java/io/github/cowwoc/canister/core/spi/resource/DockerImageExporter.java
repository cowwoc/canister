package io.github.cowwoc.canister.core.spi.resource;

import io.github.cowwoc.canister.core.resource.ImageBuilder.CompressionType;
import io.github.cowwoc.canister.core.resource.ImageBuilder.Exporter;

import java.util.StringJoiner;

public final class DockerImageExporter extends AbstractImageExporter
	implements Exporter
{
	private final String path;
	private final String context;

	/**
	 * Creates a DockerImageExporter.
	 *
	 * @param path             the output path
	 * @param name             the name of the image
	 * @param compressionType  the type of compression to use
	 * @param compressionLevel the compression level to use
	 * @param context          the Docker context into which the built image should be imported. If omitted, the
	 *                         image is imported into the same context in which the build was executed.
	 */
	public DockerImageExporter(String path, String name, CompressionType compressionType, int compressionLevel,
		String context)
	{
		super(name, compressionType, compressionLevel);
		assert path != null;
		assert context != null;
		this.path = path;
		this.context = context;
	}

	@Override
	public String getType()
	{
		return "docker";
	}

	@Override
	public boolean loadsIntoImageStore()
	{
		return path.isEmpty();
	}

	@Override
	public String toCommandLine()
	{
		StringJoiner joiner = new StringJoiner(",");
		joiner.add("type=" + getType());
		if (!path.isEmpty())
			joiner.add("dest=" + path);
		if (!name.isEmpty())
			joiner.add("name=" + name);
		if (compressionType != CompressionType.GZIP)
			joiner.add("compression=" + compressionType.toCommandLine());
		if (compressionLevel != -1)
			joiner.add("compression-level=" + compressionLevel);
		if (!context.isEmpty())
			joiner.add("context=" + context);
		return joiner.toString();
	}
}