package io.github.cowwoc.canister.docker.resource;

import io.github.cowwoc.canister.docker.id.ContextId;
import io.github.cowwoc.requirements12.annotation.CheckReturnValue;

import java.io.IOException;

/**
 * Represents a Docker context (i.e., the Docker Engine that the client communicates with).
 * <p>
 * <b>Thread Safety</b>: Implementations must be immutable and thread-safe.
 *
 * @see <a href="https://docs.docker.com/engine/manage-resources/contexts/">Docker documentation</a>
 */
public interface Context
{
	/**
	 * Returns the context's ID.
	 *
	 * @return the ID
	 */
	ContextId getId();

	/**
	 * Returns the context's name.
	 *
	 * @return the name
	 */
	String getName();

	/**
	 * Returns the context's description.
	 *
	 * @return an empty string if omitted
	 */
	String getDescription();

	/**
	 * Reloads the context's state.
	 *
	 * @return the updated state
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	@CheckReturnValue
	Context reload() throws IOException, InterruptedException;
}