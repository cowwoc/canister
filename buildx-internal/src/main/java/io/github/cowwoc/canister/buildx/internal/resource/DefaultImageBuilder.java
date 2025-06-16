package io.github.cowwoc.canister.buildx.internal.resource;

import io.github.cowwoc.canister.buildx.internal.client.InternalBuildXClient;
import io.github.cowwoc.canister.core.id.BuilderId;
import io.github.cowwoc.canister.core.id.ImageId;
import io.github.cowwoc.canister.core.resource.BuildListener;
import io.github.cowwoc.canister.core.resource.BuildListener.Output;
import io.github.cowwoc.canister.core.resource.Builder;
import io.github.cowwoc.canister.core.resource.CommandResult;
import io.github.cowwoc.canister.core.resource.DefaultBuildListener;
import io.github.cowwoc.canister.core.resource.ImageBuilder;
import io.github.cowwoc.canister.core.spi.resource.AbstractExporter;
import io.github.cowwoc.canister.core.spi.util.ParameterValidator;
import io.github.cowwoc.canister.core.spi.util.Processes;
import io.github.cowwoc.canister.core.spi.util.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

public class DefaultImageBuilder implements ImageBuilder
{
	private final InternalBuildXClient client;
	private Path dockerfile;
	private final Set<String> platforms = new HashSet<>();
	private final Set<String> tags = new HashSet<>();
	private final Set<String> cacheFrom = new HashSet<>();
	private final Set<AbstractExporter> exporters = new LinkedHashSet<>();
	private BuilderId builder;
	private BuildListener listener = new DefaultBuildListener();
	private final Logger log = LoggerFactory.getLogger(DefaultImageBuilder.class);

	/**
	 * Creates an image builder.
	 *
	 * @param client the client configuration
	 */
	public DefaultImageBuilder(InternalBuildXClient client)
	{
		assert client != null;
		this.client = client;
	}

	@Override
	public ImageBuilder dockerfile(Path dockerfile)
	{
		requireThat(dockerfile, "dockerfile").isNotNull();
		this.dockerfile = dockerfile;
		return this;
	}

	@Override
	public ImageBuilder platform(String platform)
	{
		requireThat(platform, "platform").doesNotContainWhitespace().isNotEmpty();
		this.platforms.add(platform);
		return this;
	}

	@Override
	public ImageBuilder reference(String reference)
	{
		ParameterValidator.validateImageReference(reference, "reference");
		this.tags.add(reference);
		return this;
	}

	@Override
	public ImageBuilder cacheFrom(String source)
	{
		requireThat(source, "source").doesNotContainWhitespace().isNotEmpty();
		this.cacheFrom.add(source);
		return this;
	}

	@Override
	public ImageBuilder export(Exporter exporter)
	{
		requireThat(exporter, "exporter").isNotNull();
		AbstractExporter ae = (AbstractExporter) exporter;
		this.exporters.add(ae);
		return this;
	}

	@Override
	public ImageBuilder builder(BuilderId builder)
	{
		this.builder = builder;
		return this;
	}

	@Override
	public ImageBuilder listener(BuildListener listener)
	{
		requireThat(listener, "listener").isNotNull();
		this.listener = listener;
		return this;
	}

	@Override
	public ImageId apply(String buildContext) throws IOException, InterruptedException
	{
		return apply(Path.of(buildContext));
	}

	@Override
	public ImageId apply(Path buildContext) throws IOException, InterruptedException
	{
		List<String> arguments = getArguments(buildContext);
		boolean loadsIntoImageStore = exporters.stream().anyMatch(AbstractExporter::loadsIntoImageStore);

		Path metadataJson;
		if (loadsIntoImageStore)
		{
			metadataJson = Files.createTempFile("", ".json");
			arguments.add("--metadata-file");
			arguments.add(metadataJson.toString());
		}
		else
			metadataJson = null;
		try
		{
			return client.retry(_ ->
			{
				ProcessBuilder processBuilder = client.getProcessBuilder(arguments);
				log.debug("Running: {}", processBuilder.command());
				Process process = processBuilder.start();
				listener.buildStarted(process.inputReader(), process.errorReader(), process::waitFor);
				Output output = listener.waitUntilBuildCompletes();

				int exitCode = output.exitCode();
				if (exitCode != 0)
				{
					CommandResult result = getCommandResult(processBuilder, output);
					listener.buildFailed(result);
					client.commandFailed(result);
					throw result.unexpectedResponse();
				}
				listener.buildPassed();

				if (loadsIntoImageStore)
				{
					CommandResult result = getCommandResult(processBuilder, output);
					ImageId id = client.getBuildXParser().getImageIdFromBuildOutput(result);
					assert id != null;
					return id;
				}
				return null;
			});
		}
		finally
		{
			listener.buildCompleted();
			if (metadataJson != null)
				Files.delete(metadataJson);
		}
	}

	/**
	 * @return the builder that will be used to build the image
	 */
	private Builder getBuilder() throws IOException, InterruptedException
	{
		if (builder == null)
			return client.getDefaultBuilder();
		return client.getBuilder(builder);
	}

	/**
	 * @param processBuilder the ProcessBuilder used to run the build
	 * @param output         the build output
	 * @return the CommandResult for the build
	 */
	private CommandResult getCommandResult(ProcessBuilder processBuilder, Output output)
	{
		List<String> command = List.copyOf(processBuilder.command());
		Path workingDirectory = Processes.getWorkingDirectory(processBuilder);
		return new CommandResult(command, workingDirectory, output.stdout(),
			output.stderr(), output.exitCode());
	}

	/**
	 * Returns the build's command-line arguments.
	 *
	 * @param buildContext the build context, the directory relative to which paths in the Dockerfile are
	 *                     evaluated
	 * @return the command-line arguments
	 * @throws NullPointerException if {@code buildContext} is null
	 */
	private List<String> getArguments(Path buildContext)
	{
		// Path.relativize() requires both Paths to be relative or absolute
		Path absoluteBuildContext = buildContext.toAbsolutePath().normalize();

		// https://docs.docker.com/reference/cli/docker/buildx/build/
		List<String> arguments = new ArrayList<>(2 + cacheFrom.size() + 2 + 1 + exporters.size() * 2 +
			tags.size() * 2 + 2 + 2 + 1);
		arguments.add("buildx");
		arguments.add("build");
		if (!cacheFrom.isEmpty())
		{
			for (String source : cacheFrom)
				arguments.add("--cache-from=" + source);
		}
		if (dockerfile != null)
		{
			arguments.add("--file");
			arguments.add(dockerfile.toAbsolutePath().toString());
		}
		if (!platforms.isEmpty())
			arguments.add("--platform=" + String.join(",", platforms));

		for (AbstractExporter exporter : exporters)
		{
			arguments.add("--output");
			arguments.add(exporter.toCommandLine());
		}
		for (String tag : tags)
		{
			arguments.add("--tag");
			arguments.add(tag);
		}
		if (builder != null)
		{
			arguments.add("--builder");
			arguments.add(builder.getValue());
		}
		arguments.add(absoluteBuildContext.toString());
		return arguments;
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(DefaultImageBuilder.class).
			add("platforms", platforms).
			add("tags", tags).
			toString();
	}
}