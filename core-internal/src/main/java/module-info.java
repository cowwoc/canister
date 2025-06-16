/**
 * Code that is shared between internal implementations of canister-core-api.
 */
module io.github.cowwoc.canister.core.internal
{
	requires io.github.cowwoc.canister.core.api;
	requires io.github.cowwoc.pouch.core;
	requires io.github.cowwoc.requirements12.java;
	requires io.github.cowwoc.requirements12.jackson;
	requires transitive org.slf4j;
	requires com.fasterxml.jackson.databind;
	requires org.threeten.extra;

	exports io.github.cowwoc.canister.core.internal.client to
		io.github.cowwoc.canister.buildx.internal, io.github.cowwoc.canister.docker.main,
		io.github.cowwoc.canister.docker.test;
	exports io.github.cowwoc.canister.core.internal.util to
		io.github.cowwoc.canister.buildx.main, io.github.cowwoc.canister.buildx.test,
		io.github.cowwoc.canister.docker.main, io.github.cowwoc.canister.docker.test;
	exports io.github.cowwoc.canister.core.internal.parser to
		io.github.cowwoc.canister.buildx.internal, io.github.cowwoc.canister.docker.main;
}