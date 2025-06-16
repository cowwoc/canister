package io.github.cowwoc.canister.docker.id;

import io.github.cowwoc.canister.core.id.StringId;
import io.github.cowwoc.canister.core.spi.util.ParameterValidator;

/**
 * A type-safe identifier for a Context.
 */
public final class ContextId extends StringId
{
	/**
	 * @param value the context's name
	 * @throws NullPointerException     if {@code value} is null
	 * @throws IllegalArgumentException if {@code value}'s format is invalid
	 */
	public static ContextId of(String value)
	{
		return new ContextId(value);
	}

	/**
	 * @param value the context's name
	 * @throws NullPointerException     if {@code value} is null
	 * @throws IllegalArgumentException if {@code value}'s format is invalid
	 */
	private ContextId(String value)
	{
		super(value);
		ParameterValidator.validateName(value, "value");
	}
}