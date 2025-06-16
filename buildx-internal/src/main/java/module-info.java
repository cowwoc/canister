/**
 * Code that is shared between internal implementations of {@code canister-buildx-api}.
 */
module io.github.cowwoc.canister.buildx.internal
{
	requires io.github.cowwoc.canister.core.api;
	requires io.github.cowwoc.canister.core.internal;
	requires io.github.cowwoc.canister.buildx.api;
	requires io.github.cowwoc.requirements12.java;
	requires com.fasterxml.jackson.databind;

	exports io.github.cowwoc.canister.buildx.internal.client to
		io.github.cowwoc.canister.buildx.main, io.github.cowwoc.canister.docker.main;
	exports io.github.cowwoc.canister.buildx.internal.resource to io.github.cowwoc.canister.docker.main;
	exports io.github.cowwoc.canister.buildx.internal.parser to io.github.cowwoc.canister.docker.main;
}