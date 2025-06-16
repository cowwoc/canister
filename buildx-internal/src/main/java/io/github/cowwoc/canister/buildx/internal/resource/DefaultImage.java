package io.github.cowwoc.canister.buildx.internal.resource;

import io.github.cowwoc.canister.core.id.ImageId;
import io.github.cowwoc.canister.core.resource.Image;
import io.github.cowwoc.canister.core.spi.util.ToStringBuilder;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

public class DefaultImage implements Image
{
	protected final ImageId id;

	/**
	 * Creates a reference to an image.
	 *
	 * @param id the image's ID or reference
	 * @throws NullPointerException if {@code id} is null
	 */
	public DefaultImage(ImageId id)
	{
		requireThat(id, "id").isNotNull();
		this.id = id;
	}

	@Override
	public ImageId getId()
	{
		return id;
	}

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}

	@Override
	public boolean equals(Object o)
	{
		return o instanceof Image other && other.getId().equals(id);
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(DefaultImage.class).
			add("id", id).
			toString();
	}
}