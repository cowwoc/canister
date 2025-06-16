package io.github.cowwoc.canister.docker.resource;

import io.github.cowwoc.canister.docker.api.client.DockerClient;
import io.github.cowwoc.canister.docker.id.ContainerId;

import java.util.function.Predicate;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.that;

/**
 * The properties used by the predicate in {@link DockerClient#getContainers(Predicate)}.
 *
 * @param id   the container's ID
 * @param name the container's name
 */
public record ContainerElement(ContainerId id, String name)
{
	/**
	 * Creates a container element.
	 *
	 * @param id   the container's ID
	 * @param name the container's name
	 */
	public ContainerElement
	{
		assert id != null;
		assert that(name, "name").doesNotContainWhitespace().isNotEmpty().elseThrow();
	}
}