package io.github.cowwoc.canister.core.resource;

import io.github.cowwoc.canister.core.id.BuilderId;
import io.github.cowwoc.canister.core.id.BuilderNodeId;
import io.github.cowwoc.requirements12.annotation.CheckReturnValue;

import java.io.IOException;
import java.util.List;

/**
 * Represents a service that builds images.
 * <p>
 * Two distinct kinds of errors may occur: builder-level errors, accessible via {@link Builder#getError()},
 * and node-specific errors, accessible via {@link Node#getError()}.
 * <p>
 * <b>Thread Safety</b>: Implementations must be immutable and thread-safe.
 */
public interface Builder
{
	/**
	 * Returns the build's ID.
	 *
	 * @return the ID
	 */
	BuilderId getId();

	/**
	 * Returns the name of the builder.
	 *
	 * @return the name
	 */
	String getName();

	/**
	 * Returns the nodes that the builder is on.
	 *
	 * @return the nodes
	 */
	List<Node> getNodes();

	/**
	 * Returns the driver used by the builder.
	 *
	 * @return the driver, or {@code null} if an error occurred
	 * @see <a href="https://docs.docker.com/build/builders/drivers/">Build drivers</a>
	 */
	Driver getDriver();

	/**
	 * Returns an explanation of the builder's error status.
	 *
	 * @return an empty string if there is no builder-level error
	 * @see Node#getError() node-specific errors
	 */
	String getError();

	/**
	 * Reloads the builder.
	 *
	 * @return the updated builder
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	@CheckReturnValue
	Builder reload() throws IOException, InterruptedException;

	/**
	 * Removes the builder.
	 *
	 * @return a container remover
	 */
	@CheckReturnValue
	BuilderRemover remove();

	/**
	 * Represents a node that the builder is on.
	 */
	interface Node
	{
		/**
		 * Returns the node's ID.
		 *
		 * @return the ID
		 */
		BuilderNodeId getId();

		/**
		 * Returns the name of the node
		 *
		 * @return the name
		 */
		String getName();

		/**
		 * Returns the status of the node
		 *
		 * @return the status
		 */
		Status getStatus();

		/**
		 * Returns an explanation of the builder's node-specific error.
		 *
		 * @return an empty string if there is no node-specific error
		 * @see Builder#getError() builder-level errors
		 */
		String getError();

		/**
		 * Represents the status of a builder on a node.
		 */
		enum Status
		{
			/**
			 * The builder is defined but has not been created yet.
			 * <p>
			 * For example, this status can occur before the BuildKit image has been pulled locally and the builder
			 * instance needs to be initialized.
			 */
			INACTIVE,
			/**
			 * The builder is in the process of starting up. Resources are being initialized, but it is not yet
			 * ready to accept build jobs.
			 */
			STARTING,
			/**
			 * The builder is up and ready to accept jobs.
			 */
			RUNNING,
			/**
			 * The builder is in the process of shutting down. Active jobs may still be completing.
			 */
			STOPPING,
			/**
			 * The builder exists but is not currently running.
			 */
			STOPPED,
			/**
			 * The builder is unavailable due to an error.
			 */
			ERROR
		}
	}

	/**
	 * Represents the type of build driver responsible for executing build processes.
	 * <p>
	 * Drivers define how and where builds are performed. For example, a build may run directly on the local
	 * Docker engine, inside a Docker container, in a Kubernetes cluster, or on a remote server.
	 */
	enum Driver
	{
		/**
		 * Use the local Docker engine to execute builds.
		 */
		DOCKER,

		/**
		 * Use a Docker container as the build environment.
		 */
		DOCKER_CONTAINER,

		/**
		 * Use a Kubernetes cluster to orchestrate builds.
		 */
		KUBERNETES,

		/**
		 * Use a remote build server or service.
		 */
		REMOTE
	}
}