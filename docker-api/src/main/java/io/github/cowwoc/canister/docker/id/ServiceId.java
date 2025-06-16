package io.github.cowwoc.canister.docker.id;

import io.github.cowwoc.canister.core.id.StringId;
import io.github.cowwoc.canister.core.spi.util.ParameterValidator;

/**
 * A type-safe identifier for a Service.
 */
public final class ServiceId extends StringId
{
	/**
	 * @param value the service's name
	 * @throws NullPointerException     if {@code value} is null
	 * @throws IllegalArgumentException if {@code value}'s format is invalid
	 */
	public static ServiceId of(String value)
	{
		return new ServiceId(value);
	}

	/**
	 * @param value the service's name
	 * @throws NullPointerException     if {@code value} is null
	 * @throws IllegalArgumentException if {@code value}'s format is invalid
	 */
	private ServiceId(String value)
	{
		super(value);
		ParameterValidator.validateName(value, "value");
	}
}