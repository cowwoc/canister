package io.github.cowwoc.canister.docker.api.client;

import java.io.IOException;

/**
 * Finds the {@code docker} executable.
 */
public interface DockerFinder
{
	/**
	 * Returns a client that uses the {@code docker} executable located in the {@code PATH} environment
	 * variable.
	 *
	 * @return the client
	 * @throws IOException if an I/O error occurs while building the client
	 */
	DockerClient fromPath() throws IOException;
}