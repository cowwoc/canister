package io.github.cowwoc.canister.docker.id;

import io.github.cowwoc.canister.core.id.StringId;
import io.github.cowwoc.canister.core.spi.util.ParameterValidator;

/**
 * A type-safe identifier for a Network.
 */
public final class NetworkId extends StringId
{
	/**
	 * Creates a NetworkId.
	 *
	 * @param value the network's name
	 * @return the type-safe identifier for the resource
	 * @throws IllegalArgumentException if {@code value}'s format is invalid
	 */
	public static NetworkId of(String value)
	{
		return new NetworkId(value);
	}

	/**
	 * @param value the network's name
	 * @throws NullPointerException     if {@code value} is null
	 * @throws IllegalArgumentException if {@code value}'s format is invalid
	 */
	private NetworkId(String value)
	{
		ParameterValidator.validateName(value, "value");
		super(value);
	}
}