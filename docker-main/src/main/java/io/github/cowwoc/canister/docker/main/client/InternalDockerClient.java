package io.github.cowwoc.canister.docker.main.client;

import io.github.cowwoc.canister.buildx.internal.client.InternalBuildXClient;
import io.github.cowwoc.canister.docker.api.client.DockerClient;
import io.github.cowwoc.canister.docker.main.parser.ConfigParser;
import io.github.cowwoc.canister.docker.main.parser.ContainerParser;
import io.github.cowwoc.canister.docker.main.parser.ContextParser;
import io.github.cowwoc.canister.docker.main.parser.ImageParser;
import io.github.cowwoc.canister.docker.main.parser.NetworkParser;
import io.github.cowwoc.canister.docker.main.parser.NodeParser;
import io.github.cowwoc.canister.docker.main.parser.ServiceParser;
import io.github.cowwoc.canister.docker.main.parser.SwarmParser;

/**
 * The internals of a Docker client.
 */
public interface InternalDockerClient extends InternalBuildXClient, DockerClient
{
	/**
	 * @return a {@code ContainerParser}
	 */
	ContainerParser getContainerParser();

	/**
	 * @return a {@code ConfigParser}
	 */
	ConfigParser getConfigParser();

	/**
	 * @return a {@code ImageParser}
	 */
	ImageParser getImageParser();

	/**
	 * @return a {@code ContextParser}
	 */
	ContextParser getContextParser();

	/**
	 * @return a {@code NetworkParser}
	 */
	NetworkParser getNetworkParser();

	/**
	 * @return a {@code ServiceParser}
	 */
	ServiceParser getServiceParser();

	/**
	 * @return a {@code NodeParser}
	 */
	NodeParser getNodeParser();

	/**
	 * @return a {@code SwarmParser}
	 */
	SwarmParser getSwarmParser();
}