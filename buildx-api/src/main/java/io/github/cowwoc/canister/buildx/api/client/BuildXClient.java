package io.github.cowwoc.canister.buildx.api.client;

import io.github.cowwoc.canister.core.client.Client;
import io.github.cowwoc.canister.core.id.BuilderId;
import io.github.cowwoc.canister.core.resource.Builder;
import io.github.cowwoc.canister.core.resource.BuilderCreator;
import io.github.cowwoc.canister.core.resource.BuilderRemover;
import io.github.cowwoc.canister.core.resource.ImageBuilder;
import io.github.cowwoc.requirements12.annotation.CheckReturnValue;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

/**
 * A Docker BuildX client.
 */
public interface BuildXClient extends Client
{
	@Override
	BuildXClient retryTimeout(Duration duration);

	/**
	 * Looks up the default builder.
	 *
	 * @return the builder, or {@code null} if no match is found
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	Builder getDefaultBuilder() throws IOException, InterruptedException;

	/**
	 * Looks up a builder.
	 *
	 * @param id the ID of the builder
	 * @return the builder, or {@code null} if no match is found
	 * @throws NullPointerException if {@code id} is null
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	Builder getBuilder(BuilderId id) throws IOException, InterruptedException;

	/**
	 * Returns the first builder that matches a predicate.
	 *
	 * @param predicate the predicate
	 * @return null if no match is found
	 * @throws NullPointerException  if {@code predicate} is null
	 * @throws IllegalStateException if the client is closed
	 * @throws IOException           if an I/O error occurs. These errors are typically transient, and retrying
	 *                               the request may resolve the issue.
	 * @throws InterruptedException  if the thread is interrupted while waiting for a response. This can happen
	 *                               due to shutdown signals.
	 */
	Builder getBuilder(Predicate<Builder> predicate) throws IOException, InterruptedException;

	/**
	 * Returns all the builders.
	 *
	 * @return an empty list if no match is found
	 * @throws IllegalStateException if the client is closed
	 * @throws IOException           if an I/O error occurs. These errors are typically transient, and retrying
	 *                               the request may resolve the issue.
	 * @throws InterruptedException  if the thread is interrupted while waiting for a response. This can happen
	 *                               due to shutdown signals.
	 */
	List<Builder> getBuilders() throws IOException, InterruptedException;

	/**
	 * Returns the builders that match a predicate.
	 *
	 * @param predicate the predicate
	 * @return an empty list if no match is found
	 * @throws NullPointerException  if {@code predicate} is null
	 * @throws IllegalStateException if the client is closed
	 * @throws IOException           if an I/O error occurs. These errors are typically transient, and retrying
	 *                               the request may resolve the issue.
	 * @throws InterruptedException  if the thread is interrupted while waiting for a response. This can happen
	 *                               due to shutdown signals.
	 */
	List<Builder> getBuilders(Predicate<Builder> predicate) throws IOException, InterruptedException;

	/**
	 * Creates a builder.
	 *
	 * @return a builder creator
	 */
	@CheckReturnValue
	BuilderCreator createBuilder();

	/**
	 * Removes a builder.
	 *
	 * @param id the builder's ID or name
	 * @return a builder remover
	 * @throws NullPointerException     if {@code id} is null
	 * @throws IllegalArgumentException if {@code id}'s format is invalid
	 */
	@CheckReturnValue
	BuilderRemover removeBuilder(String id);

	/**
	 * Removes a builder.
	 *
	 * @param id the builder's ID or name
	 * @return a builder remover
	 * @throws NullPointerException     if {@code id} is null
	 * @throws IllegalArgumentException if {@code id}'s format is invalid
	 */
	@CheckReturnValue
	BuilderRemover removeBuilder(BuilderId id);

	/**
	 * Blocks until at least one builder node is reachable and has the desired status.
	 * <p>
	 * If the builder already has the desired status, this method returns immediately.
	 *
	 * @param id       the ID of the builder
	 * @param status   the desired status
	 * @param deadline the absolute time by which the builder must be ready. The method will poll the builder's
	 *                 state while the current time is before this value.
	 * @return the builder
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 * @throws TimeoutException     if the deadline expires before the operation succeeds
	 */
	Builder waitUntilBuilderStatus(BuilderId id, Builder.Node.Status status, Instant deadline)
		throws IOException, InterruptedException, TimeoutException;

	/**
	 * Returns the platforms that images can be built for.
	 *
	 * @return the platforms
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	Set<String> getSupportedBuildPlatforms() throws IOException, InterruptedException;

	/**
	 * Builds an image.
	 *
	 * @return an image builder
	 */
	@CheckReturnValue
	ImageBuilder buildImage();
}