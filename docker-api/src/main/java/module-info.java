import io.github.cowwoc.canister.docker.api.client.DockerFinder;

/**
 * A Docker client.
 */
module io.github.cowwoc.canister.docker.api
{
	requires transitive io.github.cowwoc.canister.core.api;
	requires transitive io.github.cowwoc.canister.buildx.api;
	requires io.github.cowwoc.pouch.core;
	requires io.github.cowwoc.requirements12.java;

	uses DockerFinder;

	exports io.github.cowwoc.canister.docker.api.client;
	exports io.github.cowwoc.canister.docker.exception;
	exports io.github.cowwoc.canister.docker.id;
	exports io.github.cowwoc.canister.docker.resource;
}