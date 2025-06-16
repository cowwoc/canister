package io.github.cowwoc.canister.docker.id;

import io.github.cowwoc.canister.core.id.StringId;
import io.github.cowwoc.canister.core.spi.util.ParameterValidator;

/**
 * A type-safe identifier for a Node.
 */
public final class NodeId extends StringId
{
	/**
	 * Creates a NodeId.
	 *
	 * @param value the node's name
	 * @return the type-safe identifier for the resource
	 * @throws IllegalArgumentException if {@code value}'s format is invalid
	 */
	public static NodeId of(String value)
	{
		return new NodeId(value);
	}

	/**
	 * @param value the node's name
	 * @throws NullPointerException     if {@code value} is null
	 * @throws IllegalArgumentException if {@code value}'s format is invalid
	 */
	private NodeId(String value)
	{
		ParameterValidator.validateName(value, "value");
		super(value);
	}
}