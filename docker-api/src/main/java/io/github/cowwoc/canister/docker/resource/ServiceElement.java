package io.github.cowwoc.canister.docker.resource;

import io.github.cowwoc.canister.docker.api.client.DockerClient;
import io.github.cowwoc.canister.docker.id.ServiceId;

import java.util.function.Predicate;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.that;

/**
 * The properties used by the predicate in {@link DockerClient#getServices(Predicate)}.
 *
 * @param id   the node's ID
 * @param name the name of the service
 */
public record ServiceElement(ServiceId id, String name)
{
	/**
	 * Creates a node element.
	 *
	 * @param id   the node's ID
	 * @param name the name of the service
	 * @throws NullPointerException     if any of the arguments are null
	 * @throws IllegalArgumentException if {@code name}'s format is invalid
	 */
	public ServiceElement
	{
		assert id != null;
		assert that(name, "name").doesNotContainWhitespace().isNotEmpty().elseThrow();
	}
}