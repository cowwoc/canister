package io.github.cowwoc.canister.core.id;

import io.github.cowwoc.canister.core.spi.util.ParameterValidator;

/**
 * A type-safe identifier for a Builder.
 */
public final class BuilderId extends StringId
{
	/**
	 * Creates a BuilderId.
	 *
	 * @param value the name of the builder. The value must start with a letter, or digit, or underscore, and
	 *              may be followed by additional characters consisting of letters, digits, underscores, periods
	 *              or hyphens.
	 * @throws NullPointerException     if {@code value} is null
	 * @throws IllegalArgumentException if {@code value}'s format is invalid
	 */
	public static BuilderId of(String value)
	{
		return new BuilderId(value);
	}

	/**
	 * @param value the name of the builder. The value must start with a letter, or digit, or underscore, and
	 *              may be followed by additional characters consisting of letters, digits, underscores, periods
	 *              or hyphens.
	 * @throws NullPointerException     if {@code value} is null
	 * @throws IllegalArgumentException if {@code value}'s format is invalid
	 */
	private BuilderId(String value)
	{
		ParameterValidator.validateName(value, "value");
		super(value);
	}
}