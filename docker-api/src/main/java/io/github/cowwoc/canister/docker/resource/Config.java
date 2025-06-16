package io.github.cowwoc.canister.docker.resource;

import io.github.cowwoc.canister.docker.exception.NotSwarmManagerException;
import io.github.cowwoc.canister.docker.id.ConfigId;
import io.github.cowwoc.requirements12.annotation.CheckReturnValue;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Represents non-sensitive configuration stored in a Swarm.
 * <p>
 * <b>Thread Safety</b>: Implementations must be immutable and thread-safe.
 */
public interface Config
{
	/**
	 * Returns the config's ID.
	 *
	 * @return the ID
	 */
	ConfigId getId();

	/**
	 * Returns the config's name.
	 *
	 * @return the name
	 */
	String getName();

	/**
	 * Returns config's value.
	 *
	 * @return the value
	 */
	ByteBuffer getValue();

	/**
	 * Returns the String representation of the config's value.
	 *
	 * @return the value
	 */
	String getValueAsString();

	/**
	 * Reloads the config's state.
	 *
	 * @return the updated state
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	@CheckReturnValue
	Config reload() throws IOException, InterruptedException;
}