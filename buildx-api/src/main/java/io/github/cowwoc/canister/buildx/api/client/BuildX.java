package io.github.cowwoc.canister.buildx.api.client;

import java.io.IOException;
import java.util.ServiceLoader;

/**
 * Builds a new BuildXClient.
 */
public final class BuildX
{
	/**
	 * Returns a client that uses the {@code buildx} executable resolved from the {@code PATH} environment
	 * variable.
	 *
	 * @return the client
	 * @throws IOException if an I/O error occurs while building the client
	 */
	public static BuildXClient fromPath() throws IOException
	{
		BuildXFinder builder = ServiceLoader.load(BuildXFinder.class).findFirst().
			orElseThrow(() -> new IllegalStateException(
				"Unable to find a BuildXClient implementation. Did you forget to declare a dependency on " +
					"\"canister-buildx-main\"?"));
		return builder.fromPath();
	}
}