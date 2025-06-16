package io.github.cowwoc.canister.core.util;

import io.github.cowwoc.canister.core.spi.util.ToStringBuilder;

/**
 * The result of trying to create a resource.
 *
 * @param <T> the type of resource that is being created
 */
public final class CreateResult<T>
{
	/**
	 * Indicates that a new resource was created.
	 *
	 * @param <T>      the type of the resource
	 * @param resource the created resource
	 * @return the result of the create operation
	 */
	public static <T> CreateResult<T> created(T resource)
	{
		return new CreateResult<>(resource, Outcome.CREATED);
	}

	/**
	 * Indicates that an existing resource with the desired state already existed.
	 *
	 * @param <T>      the type of the resource
	 * @param resource the existing resource
	 * @return the result of the create operation
	 */
	public static <T> CreateResult<T> existed(T resource)
	{
		return new CreateResult<>(resource, Outcome.EXISTED);
	}

	/**
	 * Indicates that a conflicting resource exists and its state does not match the desired state.
	 *
	 * @param <T>      the type of the resource
	 * @param resource the conflicting resource
	 * @return the result of the create operation
	 */
	public static <T> CreateResult<T> conflictedWith(T resource)
	{
		return new CreateResult<>(resource, Outcome.CONFLICTED);
	}

	private final Outcome outcome;
	private final T resource;

	/**
	 * @param resource the returned resource
	 * @param outcome  the outcome of the operation
	 */
	private CreateResult(T resource, Outcome outcome)
	{
		assert resource != null;
		assert outcome != null;
		this.resource = resource;
		this.outcome = outcome;
	}

	/**
	 * Returns the outcome of the operation.
	 *
	 * @return the outcome
	 */
	public Outcome getOutcome()
	{
		return outcome;
	}

	/**
	 * Returns the created, existing or conflicting resource.
	 *
	 * @return the created, existing, or conflicting resource, depending on the result type
	 */
	public T getResource()
	{
		return resource;
	}

	@Override
	public String toString()
	{
		String outcome = switch (this.outcome)
		{
			case CREATED -> "created";
			case EXISTED -> "existed";
			case CONFLICTED -> "conflicted";
		};
		return new ToStringBuilder(CreateResult.class).
			add(outcome, resource).
			toString();
	}

	/**
	 * Represents the outcome of an operation involving a resource.
	 */
	public enum Outcome
	{
		/**
		 * Indicates that a new resource was successfully created.
		 */
		CREATED,

		/**
		 * Indicates that the resource already existed and matched the desired state.
		 */
		EXISTED,

		/**
		 * Indicates that the resource already existed but did not match the desired state, resulting in a
		 * conflict.
		 */
		CONFLICTED
	}
}