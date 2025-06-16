package io.github.cowwoc.canister.docker.resource;

import io.github.cowwoc.canister.docker.api.client.DockerClient;
import io.github.cowwoc.canister.docker.id.ConfigId;

import java.util.function.Predicate;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.that;

/**
 * The properties used by the predicate in {@link DockerClient#getConfigs(Predicate)}.
 *
 * @param id   the config's ID
 * @param name the config's name
 */
public record ConfigElement(ConfigId id, String name)
{
	/**
	 * Creates an element.
	 *
	 * @param id   the config's ID
	 * @param name the config's name
	 */
	public ConfigElement
	{
		assert id != null;
		assert that(name, "name").doesNotContainWhitespace().isNotEmpty().elseThrow();
	}
}