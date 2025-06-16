package io.github.cowwoc.canister.core.resource;

import java.io.IOException;

/**
 * Removes an image builder.
 */
public interface BuilderRemover
{
	/**
	 * Specifies that the BuildKit state should be preserved for reuse by a new builder with the same name.
	 * Currently, this is only supported by the {@link Builder.Driver#DOCKER_CONTAINER docker-container}
	 * driver.
	 *
	 * @return this
	 */
	BuilderRemover keepState();

	/**
	 * Removes the builder. If the builder does not exist, this method has no effect.
	 *
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	void apply() throws IOException, InterruptedException;
}