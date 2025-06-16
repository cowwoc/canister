package io.github.cowwoc.canister.docker.resource;

import io.github.cowwoc.canister.core.spi.util.ToStringBuilder;
import io.github.cowwoc.canister.docker.id.NetworkId;
import io.github.cowwoc.requirements12.annotation.CheckReturnValue;

import java.io.IOException;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * A docker network.
 * <p>
 * <b>Thread Safety</b>: Implementations must be immutable and thread-safe.
 */
public interface Network
{
	/**
	 * Returns the ID of the network.
	 *
	 * @return the ID
	 */
	NetworkId getId();

	/**
	 * Returns the name of the network.
	 *
	 * @return the name
	 */
	String getName();

	/**
	 * Returns the network's configurations.
	 *
	 * @return the configurations
	 */
	List<Configuration> getConfigurations();

	/**
	 * Reloads the network.
	 *
	 * @return the updated network
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	@CheckReturnValue
	Network reload() throws IOException, InterruptedException;

	/**
	 * A network configuration.
	 *
	 * @param subnet  the network's subnet CIDR
	 * @param gateway the network's gateway
	 */
	record Configuration(String subnet, String gateway)
	{
		/**
		 * Creates a configuration.
		 *
		 * @param subnet  the network's subnet CIDR
		 * @param gateway the network's gateway
		 * @throws NullPointerException     if any of the arguments are null
		 * @throws IllegalArgumentException if any of the arguments contain whitespace or is empty
		 */
		public Configuration
		{
			requireThat(subnet, "subnet").doesNotContainWhitespace().isNotEmpty();
			requireThat(gateway, "gateway").doesNotContainWhitespace().isNotEmpty();
		}

		@Override
		public String toString()
		{
			return new ToStringBuilder().
				add("subnet", subnet).
				add("gateway", gateway).
				toString();
		}
	}
}