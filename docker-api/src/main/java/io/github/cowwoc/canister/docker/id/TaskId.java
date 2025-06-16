package io.github.cowwoc.canister.docker.id;

import io.github.cowwoc.canister.core.id.StringId;
import io.github.cowwoc.canister.core.spi.util.ParameterValidator;

/**
 * A type-safe identifier for a Task.
 */
public final class TaskId extends StringId
{
	/**
	 * @param value the task's name
	 * @throws NullPointerException     if {@code value} is null
	 * @throws IllegalArgumentException if {@code value}'s format is invalid
	 */
	public static TaskId of(String value)
	{
		return new TaskId(value);
	}

	/**
	 * @param value the task's name
	 * @throws NullPointerException     if {@code value} is null
	 * @throws IllegalArgumentException if {@code value}'s format is invalid
	 */
	private TaskId(String value)
	{
		super(value);
		ParameterValidator.validateName(value, "value");
	}
}