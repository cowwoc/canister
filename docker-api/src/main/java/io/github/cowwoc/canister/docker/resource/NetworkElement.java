package io.github.cowwoc.canister.docker.resource;

import io.github.cowwoc.canister.core.spi.util.ParameterValidator;
import io.github.cowwoc.canister.docker.api.client.DockerClient;
import io.github.cowwoc.canister.docker.id.NetworkId;

import java.util.function.Predicate;

/**
 * The properties used by the predicate in {@link DockerClient#getNetworks(Predicate)}.
 *
 * @param id   the network's ID
 * @param name the network's name
 */
public record NetworkElement(NetworkId id, String name)
{
	/**
	 * Creates an image element.
	 *
	 * @param id   the network's ID
	 * @param name the network's name
	 * @throws NullPointerException     if any of the arguments are null
	 * @throws IllegalArgumentException if {@code name}'s format is invalid
	 */
	public NetworkElement
	{
		assert id != null;
		ParameterValidator.validateName(name, "name");
	}
}