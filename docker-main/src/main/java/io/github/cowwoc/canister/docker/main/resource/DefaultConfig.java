package io.github.cowwoc.canister.docker.main.resource;

import io.github.cowwoc.canister.core.spi.util.ToStringBuilder;
import io.github.cowwoc.canister.docker.api.client.DockerClient;
import io.github.cowwoc.canister.docker.id.ConfigId;
import io.github.cowwoc.canister.docker.main.util.Buffers;
import io.github.cowwoc.canister.docker.resource.Config;
import io.github.cowwoc.canister.docker.resource.ConfigCreator;
import io.github.cowwoc.requirements12.annotation.CheckReturnValue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

import static io.github.cowwoc.canister.docker.main.resource.DefaultConfigCreator.NAME_PATTERN;
import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class DefaultConfig implements Config
{
	private final DockerClient client;
	private final ConfigId id;
	private final String name;
	private final ByteBuffer value;

	/**
	 * Creates a config state.
	 *
	 * @param client the client configuration
	 * @param id     the config's ID
	 * @param name   the config's name
	 * @param value  the config's value
	 * @throws NullPointerException     if any of the arguments are null
	 * @throws IllegalArgumentException if {@code name} contains whitespace or is empty
	 */
	public DefaultConfig(DockerClient client, ConfigId id, String name, ByteBuffer value)
	{
		requireThat(client, "client").isNotNull();
		requireThat(id, "id").isNotNull();
		requireThat(name, "name").doesNotContainWhitespace().matches(NAME_PATTERN);
		requireThat(value, "value").isNotNull();
		requireThat(value.remaining(), "value.remaining()").isLessThanOrEqualTo(ConfigCreator.MAX_SIZE_IN_BYTES);
		this.client = client;
		this.id = id;
		this.name = name;
		this.value = Buffers.copyOf(value);
	}

	@Override
	public ConfigId getId()
	{
		return id;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public ByteBuffer getValue()
	{
		return value;
	}

	@Override
	public String getValueAsString()
	{
		return UTF_8.decode(value.duplicate()).toString();
	}

	@Override
	@CheckReturnValue
	public Config reload() throws IOException, InterruptedException
	{
		return client.getConfig(id);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id, name, value);
	}

	@Override
	public boolean equals(Object o)
	{
		return o instanceof Config other && other.getId().equals(id) && other.getValue().equals(value);
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(DefaultConfig.class).
			add("id", id).
			add("value", getValueAsString()).
			toString();
	}
}