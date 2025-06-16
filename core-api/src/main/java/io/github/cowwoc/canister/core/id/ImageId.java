package io.github.cowwoc.canister.core.id;

import io.github.cowwoc.canister.core.resource.Image;
import io.github.cowwoc.canister.core.spi.util.ParameterValidator;

/**
 * A type-safe identifier for an Image.
 */
public final class ImageId extends StringId
{
	/**
	 * Creates a ImageId.
	 *
	 * @param value the server-side identifier
	 * @return the type-safe identifier for the resource
	 * @throws IllegalArgumentException if {@code value}'s format is invalid
	 */
	public static ImageId of(String value)
	{
		return new ImageId(value);
	}

	/**
	 * @param value the image's ID or {@link Image reference}
	 * @throws NullPointerException     if {@code value} is null
	 * @throws IllegalArgumentException if {@code value}'s format is invalid
	 */
	private ImageId(String value)
	{
		super(value);
		ParameterValidator.validateImageIdOrReference(value, "value");
	}
}