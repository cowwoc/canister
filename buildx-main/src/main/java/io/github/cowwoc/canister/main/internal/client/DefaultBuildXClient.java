package io.github.cowwoc.canister.main.internal.client;

import io.github.cowwoc.canister.buildx.api.client.BuildXClient;
import io.github.cowwoc.canister.buildx.internal.client.AbstractBuildXClient;
import io.github.cowwoc.canister.buildx.internal.client.InternalBuildXClient;
import io.github.cowwoc.canister.core.internal.util.Lists;
import io.github.cowwoc.canister.core.resource.Builder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class DefaultBuildXClient extends AbstractBuildXClient
	implements InternalBuildXClient, BuildXClient
{
	private final boolean executableIsBuildX;

	/**
	 * Returns a client.
	 *
	 * @param executable the path of the {@code buildx} executable
	 * @throws NullPointerException     if any of the arguments are null
	 * @throws IllegalArgumentException if the path referenced by {@code executable} does not exist or is not an
	 *                                  executable file
	 */
	public DefaultBuildXClient(Path executable) throws IOException
	{
		super(executable);
		String filename = executable.getFileName().toString();
		this.executableIsBuildX = filename.contains("buildx");
	}

	@Override
	public ProcessBuilder getProcessBuilder(List<String> arguments)
	{
		List<String> command = new ArrayList<>(arguments.size() + 3);
		if (executableIsBuildX)
		{
			// Remove "buildx" from the arguments as it will be replaced by the executable
			arguments = arguments.subList(1, arguments.size());
		}
		command.add(executable.toString());
		command.addAll(arguments);
		return new ProcessBuilder(command);
	}

	@Override
	public List<Object> getAll(Predicate<? super Class<?>> typeFilter, Predicate<Object> resourceFilter)
		throws IOException, InterruptedException
	{
		Set<Class<?>> types = Set.of(Builder.class).stream().filter(typeFilter).collect(Collectors.toSet());
		if (types.isEmpty())
			return List.of();
		return Lists.combine(getBuilders(resourceFilter::test));
	}
}