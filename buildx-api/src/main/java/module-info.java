import io.github.cowwoc.canister.buildx.api.client.BuildXFinder;

/**
 * The BuildX API.
 */
module io.github.cowwoc.canister.buildx.api
{
	requires transitive io.github.cowwoc.canister.core.api;
	requires io.github.cowwoc.requirements12.annotation;
	requires io.github.cowwoc.requirements12.java;

	uses BuildXFinder;

	exports io.github.cowwoc.canister.buildx.api.client;
}