package io.github.cowwoc.canister.core.internal.client;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.TimeoutException;

/**
 * Represents an operation that may fail due to intermittent I/O errors.
 *
 * @param <V> the type of value returned by the operation
 */
public interface Operation<V>
{
	/**
	 * Runs the operation.
	 *
	 * @param deadline the absolute time by which the operation must succeed. The method may only retry failed
	 *                 operations while the current time is before this value.
	 * @return the value returned by the operation
	 * @throws NullPointerException if {@code deadline} is null
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	V run(Instant deadline) throws InterruptedException, IOException, TimeoutException;
}