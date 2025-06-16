package io.github.cowwoc.canister.buildx.api.client;

import java.io.IOException;

/**
 * Finds the {@code buildx} executable.
 */
public interface BuildXFinder
{
	/**
	 * Returns a client that uses the {@code docker} executable located in the {@code PATH} environment
	 * variable.
	 *
	 * @return the client
	 * @throws IOException if an I/O error occurs while building the client
	 */
	BuildXClient fromPath() throws IOException;
}