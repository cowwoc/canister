package io.github.cowwoc.canister.buildx.internal.client;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.cowwoc.canister.buildx.api.client.BuildXClient;
import io.github.cowwoc.canister.buildx.internal.parser.AbstractBuildXParser;
import io.github.cowwoc.canister.buildx.internal.parser.BuildXParser;
import io.github.cowwoc.canister.buildx.internal.resource.DefaultBuilderCreator;
import io.github.cowwoc.canister.buildx.internal.resource.DefaultBuilderRemover;
import io.github.cowwoc.canister.buildx.internal.resource.DefaultImage;
import io.github.cowwoc.canister.buildx.internal.resource.DefaultImageBuilder;
import io.github.cowwoc.canister.core.id.BuilderId;
import io.github.cowwoc.canister.core.id.ImageId;
import io.github.cowwoc.canister.core.internal.client.CommandRunner;
import io.github.cowwoc.canister.core.internal.client.InternalClient;
import io.github.cowwoc.canister.core.internal.client.Operation;
import io.github.cowwoc.canister.core.resource.Builder;
import io.github.cowwoc.canister.core.resource.Builder.Node.Status;
import io.github.cowwoc.canister.core.resource.BuilderCreator;
import io.github.cowwoc.canister.core.resource.BuilderRemover;
import io.github.cowwoc.canister.core.resource.CommandResult;
import io.github.cowwoc.canister.core.resource.Image;
import io.github.cowwoc.canister.core.resource.ImageBuilder;
import io.github.cowwoc.canister.core.spi.util.Processes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.regex.Matcher;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Common implementation shared by all {@code InternalClient}s.
 */
@SuppressWarnings("PMD.MoreThanOneLogger")
public abstract class AbstractBuildXClient implements InternalClient, InternalBuildXClient
{
	private final static Duration SLEEP_DURATION = java.time.Duration.ofMillis(100);
	protected final static ThreadFactory THREAD_FACTORY = Thread.ofVirtual().name("docker-", 1).factory();
	/**
	 * The path of the command-line executable.
	 */
	protected final Path executable;
	private final JsonMapper jsonMapper = JsonMapper.builder().build();
	private Duration retryTimeout = Duration.ofSeconds(30);
	@SuppressWarnings("this-escape")
	private final BuildXParser buildXParser = new BuildXParser(this);
	private boolean closed;
	protected final Logger log = LoggerFactory.getLogger(AbstractBuildXClient.class);

	/**
	 * Creates an AbstractInternalClient.
	 *
	 * @param executable the path of the command-line executable
	 * @throws NullPointerException     if {@code executable} is null
	 * @throws IllegalArgumentException if the path referenced by {@code executable} does not exist or is not an
	 *                                  executable file
	 * @throws IOException              if an I/O error occurs while reading {@code executable}'s attributes
	 */
	protected AbstractBuildXClient(Path executable) throws IOException
	{
		requireThat(executable, "executable").exists().isRegularFile().isExecutable();
		this.executable = executable;
	}

	@Override
	public BuildXClient retryTimeout(Duration duration)
	{
		requireThat(duration, "duration").isNotNull();
		retryTimeout = duration;
		return this;
	}

	@Override
	public Duration getRetryTimeout()
	{
		return retryTimeout;
	}

	@Override
	public <V> V retry(Operation<V> operation) throws IOException, InterruptedException
	{
		try
		{
			return retry(operation, Instant.now().plus(getRetryTimeout()));
		}
		catch (TimeoutException e)
		{
			throw new AssertionError("An operation without a timeout threw a TimeoutException", e);
		}
	}

	@Override
	public <V> V retry(Operation<V> operation, Instant deadline)
		throws IOException, InterruptedException, TimeoutException
	{
		while (true)
		{
			try
			{
				return operation.run(deadline);
			}
			catch (FileNotFoundException e)
			{
				// Failures that are assumed to be non-intermittent
				throw e;
			}
			catch (IOException e)
			{
				// WORKAROUND: https://github.com/moby/moby/issues/50160
				if (!sleepBeforeRetry(deadline, e))
					throw e;
			}
			catch (IllegalStateException e)
			{
				if (e.getClass().getName().
					equals("io.github.cowwoc.canister.core.exception.UnsupportedExporterException"))
				{
					// Surprisingly, the following error occurs intermittently under load:
					//
					// ERROR: failed to build: docker exporter does not currently support exporting manifest lists
					if (!sleepBeforeRetry(deadline, e))
						throw e;
				}
				else
					throw e;
			}
		}
	}

	/**
	 * Checks if a timeout occurred.
	 *
	 * @param deadline the absolute time by which the operation must succeed. The method will retry failed
	 *                 operations while the current time is before this value.
	 * @param t        the exception that was thrown
	 * @return {@code true} if the operation may be retried
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean sleepBeforeRetry(Instant deadline, Throwable t) throws InterruptedException
	{
		Instant nextRetry = Instant.now().plus(SLEEP_DURATION);
		if (nextRetry.isAfter(deadline))
			return false;
		Thread.sleep(SLEEP_DURATION);
		log.debug("Retrying after sleep", t);
		return true;
	}

	/**
	 * Checks if a timeout occurred.
	 *
	 * @param deadline the absolute time by which the operation must succeed. The method will retry failed
	 *                 operations while the current time is before this value.
	 * @return {@code true} if the operation may be retried
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	protected boolean sleepBeforeRetry(Instant deadline) throws InterruptedException
	{
		Instant nextRetry = Instant.now().plus(SLEEP_DURATION);
		if (nextRetry.isAfter(deadline))
			return false;
		Thread.sleep(SLEEP_DURATION);
		log.debug("Retrying after sleep");
		return true;
	}

	@Override
	public JsonMapper getJsonMapper()
	{
		ensureOpen();
		return jsonMapper;
	}

	@Override
	public CommandResult run(List<String> arguments) throws IOException, InterruptedException
	{
		ensureOpen();
		return new CommandRunner(getProcessBuilder(arguments)).
			failureHandler(this::commandFailed).
			apply();
	}

	@Override
	public CommandResult run(List<String> arguments, ByteBuffer stdin) throws IOException, InterruptedException
	{
		ensureOpen();
		return new CommandRunner(getProcessBuilder(arguments)).
			stdin(stdin).
			failureHandler(this::commandFailed).
			apply();
	}

	@Override
	public void commandFailed(CommandResult result) throws IOException
	{
		String stderr = result.stderr();
		Matcher matcher = AbstractBuildXParser.CONNECTION_RESET.matcher(stderr);
		if (matcher.matches())
			throw new IOException("Connection reset trying to connect to " + matcher.group(1));
		if (!Processes.isWindows())
			return;
		matcher = AbstractBuildXParser.FILE_LOCKED.matcher(stderr);
		if (matcher.matches())
			throw new IOException("File locked by another process: " + matcher.group(1));
		matcher = AbstractBuildXParser.ACCESS_DENIED.matcher(stderr);
		if (matcher.matches())
			throw new IOException("File locked by another process: " + matcher.group(1));
	}

	@Override
	public BuildXParser getBuildXParser()
	{
		return buildXParser;
	}

	@Override
	public Builder getDefaultBuilder() throws IOException, InterruptedException
	{
		// https://docs.docker.com/reference/cli/docker/buildx/inspect/
		List<String> arguments = new ArrayList<>(2);
		arguments.add("buildx");
		arguments.add("inspect");
		CommandResult result = retry(_ -> run(arguments));
		return getBuildXParser().builderFromServer(result);
	}

	@Override
	public BuilderCreator createBuilder()
	{
		ensureOpen();
		return new DefaultBuilderCreator(this);
	}

	@Override
	public BuilderRemover removeBuilder(String id)
	{
		return removeBuilder(BuilderId.of(id));
	}

	@Override
	public BuilderRemover removeBuilder(BuilderId id)
	{
		ensureOpen();
		return new DefaultBuilderRemover(this, id);
	}

	@Override
	public List<Builder> getBuilders() throws IOException, InterruptedException
	{
		ensureOpen();
		return getBuilders(_ -> true);
	}

	@Override
	public List<Builder> getBuilders(Predicate<Builder> predicate) throws IOException, InterruptedException
	{
		ensureOpen();
		// https://docs.docker.com/reference/cli/docker/buildx/inspect/
		List<String> arguments = List.of("buildx", "ls", "--format", "json");
		CommandResult result = retry(_ -> run(arguments));
		return getBuildXParser().getBuilders(result).stream().filter(predicate).toList();
	}

	@Override
	public Builder getBuilder(BuilderId id) throws IOException, InterruptedException
	{
		ensureOpen();
		requireThat(id, "id").isNotNull();

		// https://docs.docker.com/reference/cli/docker/buildx/inspect/
		List<String> arguments = new ArrayList<>(3);
		arguments.add("buildx");
		arguments.add("inspect");
		arguments.add(id.getValue());
		CommandResult result = retry(_ -> run(arguments));
		return getBuildXParser().builderFromServer(result);
	}

	@Override
	public Builder getBuilder(Predicate<Builder> predicate) throws IOException, InterruptedException
	{
		List<Builder> builders = getBuilders(predicate);
		if (builders.isEmpty())
			return null;
		return builders.getFirst();
	}

	@Override
	@SuppressWarnings("BusyWait")
	public Builder waitUntilBuilderStatus(BuilderId id, Builder.Node.Status status, Instant deadline)
		throws IOException, InterruptedException, TimeoutException
	{
		ensureOpen();
		while (true)
		{
			Builder builder = getBuilder(id);

			Instant now = Instant.now();
			if (builder == null)
			{
				log.debug("builder == null");
				if (now.isAfter(deadline))
					throw new TimeoutException("Builder not found");
			}
			else if (builder.getNodes().isEmpty())
			{
				log.debug("builder.getNodes() is empty");
				if (now.isAfter(deadline))
					throw new TimeoutException("Builder not found");
			}
			else
			{
				Builder.Node firstNode = builder.getNodes().getFirst();
				if (firstNode.getStatus() == status)
					return builder;
				log.debug("builder.status: {}", firstNode.getStatus());
				if (now.isAfter(deadline))
				{
					StringBuilder message = new StringBuilder("Default builder " + builder.getName() +
						" has a state of " + firstNode.getStatus());
					if (firstNode.getStatus() == Status.ERROR)
					{
						message.append("\n" +
							"Error: ").append(firstNode.getError());
					}
					throw new TimeoutException(message.toString());
				}
			}
			Thread.sleep(100);
		}
	}

	@Override
	public Set<String> getSupportedBuildPlatforms() throws IOException, InterruptedException
	{
		ensureOpen();
		// https://docs.docker.com/reference/cli/docker/buildx/inspect/
		List<String> arguments = List.of("buildx", "inspect");
		CommandResult result = retry(_ -> run(arguments));
		return getBuildXParser().getSupportedBuildPlatforms(result);
	}

	@Override
	public Image getImage(String id) throws IOException, InterruptedException
	{
		return getImage(ImageId.of(id));
	}

	@Override
	public Image getImage(ImageId id) throws IOException, InterruptedException
	{
		ensureOpen();
		return new DefaultImage(id);
	}

	@Override
	public ImageBuilder buildImage()
	{
		ensureOpen();
		return new DefaultImageBuilder(this);
	}

	/**
	 * Ensures that the client is open.
	 *
	 * @throws IllegalStateException if the client is closed
	 */
	public void ensureOpen()
	{
		if (isClosed())
			throw new IllegalStateException("client was closed");
	}

	@Override
	public boolean isClosed()
	{
		return closed;
	}

	@Override
	public void close()
	{
		this.closed = true;
	}
}