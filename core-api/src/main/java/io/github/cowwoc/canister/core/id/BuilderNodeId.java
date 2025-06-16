package io.github.cowwoc.canister.core.id;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * A type-safe identifier for this type of resource.
 */
public final class BuilderNodeId extends StringId
{
	/**
	 * Creates a BuilderNodeId.
	 *
	 * @param value the server-side identifier
	 * @return the type-safe identifier for the resource
	 * @throws IllegalArgumentException if {@code value} contains whitespace or is empty
	 */
	public static BuilderNodeId of(String value)
	{
		return new BuilderNodeId(value);
	}

	/**
	 * @param value the server-side identifier
	 * @throws NullPointerException     if {@code value} is null
	 * @throws IllegalArgumentException if {@code value} contains whitespace or is empty
	 */
	private BuilderNodeId(String value)
	{
		requireThat(value, "value").doesNotContainWhitespace().isNotEmpty();
		super(value);
	}
}