import io.github.cowwoc.canister.buildx.api.client.BuildXFinder;
import io.github.cowwoc.canister.docker.api.client.DockerFinder;
import io.github.cowwoc.canister.docker.main.client.DefaultDockerFinder;

/**
 * The main implementation of {@code canister-docker-api}.
 */
module io.github.cowwoc.canister.docker.main
{
	requires transitive io.github.cowwoc.canister.core.api;
	requires transitive io.github.cowwoc.canister.buildx.api;
	requires io.github.cowwoc.pouch.core;
	requires io.github.cowwoc.requirements12.jackson;
	requires com.fasterxml.jackson.databind;
	requires org.slf4j;
	requires io.github.cowwoc.canister.docker.api;
	requires io.github.cowwoc.canister.core.internal;
	requires io.github.cowwoc.canister.buildx.internal;

	exports io.github.cowwoc.canister.docker.main.util to io.github.cowwoc.canister.docker.test;
	exports io.github.cowwoc.canister.docker.main.client to io.github.cowwoc.canister.docker.test;
	exports io.github.cowwoc.canister.docker.main.resource to io.github.cowwoc.canister.docker.test;
	exports io.github.cowwoc.canister.docker.main.parser to io.github.cowwoc.canister.docker.test;

	provides BuildXFinder with DefaultDockerFinder;
	provides DockerFinder with DefaultDockerFinder;
}