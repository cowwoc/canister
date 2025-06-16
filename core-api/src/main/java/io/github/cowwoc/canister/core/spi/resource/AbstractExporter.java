package io.github.cowwoc.canister.core.spi.resource;

/**
 * Code common to all exporters.
 */
public abstract sealed class AbstractExporter permits AbstractImageExporter, DefaultContentsExporter
{
	/**
	 * Returns the type of the exporter.
	 *
	 * @return the type
	 */
	protected abstract String getType();

	/**
	 * Indicates whether the exporter automatically loads the generated image into an image store, such as the
	 * Docker Engine or a remote image registry.
	 *
	 * @return {@code true} if the image will be loaded into an image store
	 */
	public abstract boolean loadsIntoImageStore();

	/**
	 * Returns the command-line representation of this option.
	 *
	 * @return the command-line value
	 */
	public abstract String toCommandLine();

	@Override
	public int hashCode()
	{
		return toCommandLine().hashCode();
	}

	@Override
	public boolean equals(Object o)
	{
		return o instanceof AbstractExporter other && other.toCommandLine().equals(toCommandLine());
	}
}