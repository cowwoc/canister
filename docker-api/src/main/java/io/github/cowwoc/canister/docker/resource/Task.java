package io.github.cowwoc.canister.docker.resource;

import io.github.cowwoc.canister.docker.id.TaskId;
import io.github.cowwoc.requirements12.annotation.CheckReturnValue;

import java.io.IOException;

/**
 * A unit of work (container) that executes on a Swarm node.
 * <p>
 * <b>Thread Safety</b>: This class is immutable and thread-safe.
 */
public interface Task
{
	/**
	 * Returns the task's ID.
	 *
	 * @return the ID
	 */
	TaskId getId();

	/**
	 * Returns the task's name.
	 *
	 * @return the name
	 */
	String getName();

	/**
	 * Returns the task's state.
	 *
	 * @return the state
	 */
	State getState();

	/**
	 * Reloads the task.
	 *
	 * @return the updated task
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	@CheckReturnValue
	Task reload() throws IOException, InterruptedException;

	/**
	 * A task's states.
	 */
	public enum State
	{
		// Based on https://github.com/moby/swarmkit/blob/8c19597365549f6966a5021d2ea46810099aae28/api/types.proto#L516
		/**
		 * The task has been created but has not yet been assigned to any node.
		 */
		NEW,
		/**
		 * The task has been submitted to the scheduler, but no node has been selected yet.
		 */
		PENDING,
		/**
		 * The task has been assigned to a node but not yet acknowledged by it.
		 */
		ASSIGNED,
		/**
		 * The node has accepted the task and will begin preparing it.
		 */
		ACCEPTED,
		/**
		 * The task is preparing its environment (e.g., downloading images, setting up mounts or networks).
		 */
		PREPARING,
		/**
		 * The task is fully prepared and ready to start execution.
		 */
		READY,
		/**
		 * The task's container is starting.
		 */
		STARTING,
		/**
		 * The task's container is actively running.
		 */
		RUNNING,
		/**
		 * The task has finished execution successfully (zero exit code).
		 */
		COMPLETE,
		/**
		 * The task has been shut down due to update, scaling, or other scheduler decisions.
		 */
		SHUTDOWN,
		/**
		 * The task has failed due to an error or non-zero exit code.
		 */
		FAILED,
		/**
		 * The task was rejected by the scheduler or an agent (e.g., due to constraints or failures).
		 */
		REJECTED,
		/**
		 * The task is marked for deletion and will be removed shortly.
		 */
		REMOVE,
		/**
		 * The task was orphaned (e.g., due to node unavailability or loss of manager connectivity).
		 */
		ORPHANED
	}
}