package io.github.cowwoc.canister.buildx.internal.client;

import io.github.cowwoc.canister.buildx.api.client.BuildXClient;
import io.github.cowwoc.canister.buildx.internal.parser.BuildXParser;
import io.github.cowwoc.canister.core.id.ImageId;
import io.github.cowwoc.canister.core.internal.client.InternalCommandLineClient;
import io.github.cowwoc.canister.core.resource.Image;

import java.io.IOException;

/**
 * The internals shared by all clients.
 */
public interface InternalBuildXClient extends InternalCommandLineClient, BuildXClient
{
	/**
	 * @return a {@code BuildXParser}
	 */
	BuildXParser getBuildXParser();

	/**
	 * Looks up an image.
	 *
	 * @param id the image's ID or {@link Image reference}
	 * @return the image
	 * @throws NullPointerException     if {@code id} is null
	 * @throws IllegalArgumentException if {@code id}'s format is invalid
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	Image getImage(String id) throws IOException, InterruptedException;

	/**
	 * Looks up an image.
	 *
	 * @param id the image's ID or {@link Image reference}
	 * @return the image
	 * @throws NullPointerException if {@code id} is null
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	Image getImage(ImageId id) throws IOException, InterruptedException;
}