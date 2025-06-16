package io.github.cowwoc.canister.docker.resource;

import io.github.cowwoc.canister.docker.exception.NotSwarmManagerException;
import io.github.cowwoc.canister.docker.id.ServiceId;

import java.io.IOException;
import java.util.List;

/**
 * Represents a high-level definition of a task or application you want to run in a Swarm. It defines:
 * <ul>
 * <li>The desired state, such as the number of replicas (containers).</li>
 * <li>The container image to use.</li>
 * <li>The command to run.</li>
 * <li>Ports to expose.</li>
 * <li>Update and restart policies, etc.</li>
 * </ul>
 */
public interface Service
{
	/**
	 * Returns the service's ID.
	 *
	 * @return the ID
	 */
	ServiceId getId();

	/**
	 * Returns the service's name.
	 *
	 * @return the name
	 */
	String getName();

	/**
	 * Lists a service's tasks.
	 *
	 * @return the tasks
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	List<Task> listTasks() throws IOException, InterruptedException;
}