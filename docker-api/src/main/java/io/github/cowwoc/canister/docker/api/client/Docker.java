package io.github.cowwoc.canister.docker.api.client;

import java.io.IOException;
import java.util.ServiceLoader;

/**
 * Builds a new DockerClient.
 */
public final class Docker
{
	/**
	 * Returns a client that uses the {@code docker} executable resolved from the {@code PATH} environment
	 * variable.
	 *
	 * @return the client
	 * @throws IOException if an I/O error occurs while building the client
	 */
	public static DockerClient fromPath() throws IOException
	{
		DockerFinder builder = ServiceLoader.load(DockerFinder.class).findFirst().
			orElseThrow(() -> new IllegalStateException(
				"Unable to find a BuildXClient implementation. Did you forget to declare a dependency on " +
					"\"canister-buildx-main\"?"));
		return builder.fromPath();
	}
}