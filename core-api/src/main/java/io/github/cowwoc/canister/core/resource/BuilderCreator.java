package io.github.cowwoc.canister.core.resource;

import io.github.cowwoc.canister.core.exception.ContextNotFoundException;
import io.github.cowwoc.canister.core.exception.ResourceInUseException;
import io.github.cowwoc.canister.core.id.BuilderId;
import io.github.cowwoc.requirements12.annotation.CheckReturnValue;

import java.io.IOException;
import java.util.List;

/**
 * Creates an image builder.
 */
public interface BuilderCreator
{
	/**
	 * Sets the builder's name.
	 *
	 * @param name the name
	 * @return this
	 * @throws NullPointerException     if {@code name} is null
	 * @throws IllegalArgumentException if {@code name}'s format is invalid
	 */
	BuilderCreator name(String name);

	/**
	 * Starts the builder immediately after creation. By default, the builder is started lazily when it handles
	 * its first request.
	 * <p>
	 * Eager startup helps surface configuration issues early.
	 *
	 * @return this
	 */
	BuilderCreator startEagerly();

	/**
	 * Sets the builder's driver.
	 *
	 * @param driver the driver
	 * @return this
	 * @throws NullPointerException if {@code driver} is null
	 */
	BuilderCreator driver(Driver driver);

	/**
	 * Sets the context to create the builder on. By default, the current Docker context is used.
	 *
	 * @param context the name of the context
	 * @return this
	 * @throws NullPointerException     if {@code context} is null
	 * @throws IllegalArgumentException if {@code context}'s format is invalid
	 */
	BuilderCreator context(String context);

	/**
	 * Creates the builder.
	 *
	 * @return the builder ID
	 * @throws ContextNotFoundException if the Docker context cannot be found or resolved
	 * @throws ResourceInUseException   if the builder already exists
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	BuilderId apply() throws IOException, InterruptedException;

	/**
	 * The backend used to execute builds.
	 */
	sealed interface Driver
	{
		/**
		 * Runs builds in an isolated BuildKit container. Enables advanced features like multi-node builds, better
		 * caching, and exporting to OCI.
		 *
		 * @return the driver
		 */
		@CheckReturnValue
		static DockerContainerDriverBuilder dockerContainer()
		{
			return new DockerContainerDriverBuilder();
		}

		/**
		 * Runs BuildKit inside a Kubernetes cluster.
		 *
		 * @return the driver
		 */
		@CheckReturnValue
		static KubernetesDriverBuilder kubernetes()
		{
			return new KubernetesDriverBuilder();
		}

		/**
		 * Connects to an existing BuildKit instance.
		 *
		 * @return the driver
		 */
		@CheckReturnValue
		static RemoteDriverBuilder remote()
		{
			return new RemoteDriverBuilder();
		}

		/**
		 * Returns the command-line representation of this option.
		 *
		 * @return the command-line options
		 */
		List<String> toCommandLine();
	}

	/**
	 * Builds a BuildKit environment in a dedicated Docker container.
	 */
	final class DockerContainerDriverBuilder
	{
		/**
		 * Builds the driver.
		 *
		 * @return the driver
		 */
		public Driver build()
		{
			return new DriverAdapter();
		}

		private static final class DriverAdapter implements Driver
		{
			@Override
			public List<String> toCommandLine()
			{
				return List.of("--driver=docker-container");
			}
		}
	}

	/**
	 * Builds a BuildKit environment inside a Kubernetes cluster.
	 */
	final class KubernetesDriverBuilder
	{
		/**
		 * Builds the driver.
		 *
		 * @return the driver
		 */
		public Driver build()
		{
			return new DriverAdapter();
		}

		private static final class DriverAdapter implements Driver
		{
			@Override
			public List<String> toCommandLine()
			{
				return List.of("--driver=kubernetes");
			}
		}
	}

	/**
	 * Connects to an already running BuildKit instance.
	 */
	final class RemoteDriverBuilder
	{
		/**
		 * Builds the driver.
		 *
		 * @return the driver
		 */
		public Driver build()
		{
			return new DriverAdapter();
		}

		private static final class DriverAdapter implements Driver
		{
			@Override
			public List<String> toCommandLine()
			{
				return List.of("--driver=remote");
			}
		}
	}
}