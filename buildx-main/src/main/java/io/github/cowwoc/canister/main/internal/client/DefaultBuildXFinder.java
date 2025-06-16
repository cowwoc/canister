package io.github.cowwoc.canister.main.internal.client;

import io.github.cowwoc.canister.buildx.api.client.BuildXClient;
import io.github.cowwoc.canister.buildx.api.client.BuildXFinder;
import io.github.cowwoc.canister.core.internal.util.Paths;
import io.github.cowwoc.pouch.core.ConcurrentLazyReference;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;

public final class DefaultBuildXFinder implements BuildXFinder
{
	private static final ConcurrentLazyReference<Path> EXECUTABLE_FROM_PATH =
		ConcurrentLazyReference.create(() ->
		{
			Path path = Paths.searchPath(List.of("buildx", "docker-buildx"));
			if (path == null)
				path = Paths.searchPath(List.of("docker"));
			if (path == null)
				throw new UncheckedIOException(new IOException("Could not find buildx or docker on the PATH"));
			return path;
		});

	/**
	 * @return the path of the {@code buildx} or {@code docker} executable located in the {@code PATH}
	 * 	environment variable
	 */
	private static Path getExecutableFromPath() throws IOException
	{
		try
		{
			return EXECUTABLE_FROM_PATH.getValue();
		}
		catch (UncheckedIOException e)
		{
			throw e.getCause();
		}
	}

	@Override
	public BuildXClient fromPath() throws IOException
	{
		return new DefaultBuildXClient(getExecutableFromPath());
	}
}