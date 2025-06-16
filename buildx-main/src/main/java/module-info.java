import io.github.cowwoc.canister.buildx.api.client.BuildXFinder;
import io.github.cowwoc.canister.main.internal.client.DefaultBuildXFinder;

/**
 * The main implementation of {@code canister-buildx-api}.
 */
module io.github.cowwoc.canister.buildx.main
{
	requires transitive io.github.cowwoc.canister.core.api;
	requires io.github.cowwoc.canister.core.internal;
	requires io.github.cowwoc.canister.buildx.api;
	requires io.github.cowwoc.canister.buildx.internal;
	requires io.github.cowwoc.pouch.core;
	requires io.github.cowwoc.requirements12.annotation;
	requires io.github.cowwoc.requirements12.java;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.databind;

	exports io.github.cowwoc.canister.main.internal.client to io.github.cowwoc.canister.buildx.test;

	provides BuildXFinder with DefaultBuildXFinder;
}