/**
 * Code that is common to all modules.
 */
module io.github.cowwoc.canister.core.api
{
	requires io.github.cowwoc.pouch.core;
	requires io.github.cowwoc.requirements12.java;
	requires transitive org.slf4j;

	exports io.github.cowwoc.canister.core.client;
	exports io.github.cowwoc.canister.core.exception;
	exports io.github.cowwoc.canister.core.id;
	exports io.github.cowwoc.canister.core.resource;
	exports io.github.cowwoc.canister.core.util;
	exports io.github.cowwoc.canister.core.spi.resource;
	exports io.github.cowwoc.canister.core.spi.util;
}