package io.github.cowwoc.canister.docker.main.resource;

import io.github.cowwoc.canister.core.id.ImageId;
import io.github.cowwoc.canister.core.resource.CommandResult;
import io.github.cowwoc.canister.core.spi.util.ParameterValidator;
import io.github.cowwoc.canister.core.spi.util.ToStringBuilder;
import io.github.cowwoc.canister.docker.main.client.InternalDockerClient;
import io.github.cowwoc.canister.docker.resource.DockerImage;
import io.github.cowwoc.canister.docker.resource.ImagePusher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Default implementation of {@code ImagePusher}.
 */
public final class DefaultImagePusher implements ImagePusher
{
	private final InternalDockerClient client;
	private final String reference;
	private String platform = "";

	/**
	 * Creates an image pusher.
	 *
	 * @param reference the reference to push. For example, {@code docker.io/nasa/rocket-ship}. The image must
	 *                  be present in the local image store with the same name.
	 * @param client    the client configuration
	 * @throws NullPointerException     if {@code reference} is null
	 * @throws IllegalArgumentException if {@code reference}'s format is invalid
	 */
	public DefaultImagePusher(InternalDockerClient client, String reference)
	{
		assert client != null;
		ParameterValidator.validateImageReference(reference, "reference");
		this.client = client;
		this.reference = reference;
	}

	@Override
	public ImagePusher platform(String platform)
	{
		requireThat(platform, "platform").doesNotContainWhitespace();
		this.platform = platform;
		return this;
	}

	@Override
	public DockerImage apply() throws IOException, InterruptedException
	{
		// https://docs.docker.com/reference/cli/docker/image/push/
		List<String> arguments = new ArrayList<>(5);
		arguments.add("image");
		arguments.add("push");
		if (!platform.isEmpty())
		{
			arguments.add("--platform");
			arguments.add(platform);
		}
		arguments.add(reference);
		CommandResult result = client.retry(_ -> client.run(arguments));
		client.getImageParser().push(result);
		return client.getImage(ImageId.of(reference));
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(DefaultImagePusher.class).
			add("reference", reference).
			add("platform", platform).
			toString();
	}
}