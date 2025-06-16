package io.github.cowwoc.canister.docker.id;

import io.github.cowwoc.canister.core.id.StringId;
import io.github.cowwoc.canister.core.spi.util.ParameterValidator;

/**
 * A type-safe identifier for a Config.
 */
public final class ConfigId extends StringId
{
	/**
	 * Creates a ConfigId.
	 *
	 * @param value the config's name
	 * @return the type-safe identifier for the resource
	 * @throws IllegalArgumentException if {@code value}'s format is invalid
	 */
	public static ConfigId of(String value)
	{
		return new ConfigId(value);
	}

	/**
	 * @param value the config's name
	 * @throws NullPointerException     if {@code value} is null
	 * @throws IllegalArgumentException if {@code value}'s format is invalid
	 */
	private ConfigId(String value)
	{
		ParameterValidator.validateName(value, "value");
		super(value);
	}
}