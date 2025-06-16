package io.github.cowwoc.canister.docker.id;

import io.github.cowwoc.canister.core.id.StringId;
import io.github.cowwoc.canister.core.spi.util.ParameterValidator;

/**
 * A type-safe identifier for a container.
 */
public final class ContainerId extends StringId
{
	/**
	 * @param value the container's ID or name
	 * @throws NullPointerException     if {@code value} is null
	 * @throws IllegalArgumentException if {@code value}'s format is invalid
	 */
	public static ContainerId of(String value)
	{
		return new ContainerId(value);
	}

	/**
	 * @param value the container's ID or name
	 * @throws NullPointerException     if {@code value} is null
	 * @throws IllegalArgumentException if {@code value}'s format is invalid
	 */
	private ContainerId(String value)
	{
		super(value);
		ParameterValidator.validateContainerIdOrName(value, "value");
	}
}
