package io.github.cowwoc.canister.core.resource;

import java.nio.file.Path;
import java.util.List;

/**
 * Represents the result of executing a command.
 *
 * @param command          the command that was executed
 * @param workingDirectory the directory that the command was executed from
 * @param stdout           the standard output stream of the command
 * @param stderr           the standard error stream of the command
 * @param exitCode         the exit code returned by the command
 */
public record CommandResult(List<String> command, Path workingDirectory, String stdout, String stderr,
                            int exitCode)
{
	/**
	 * @param command          the command that was executed
	 * @param workingDirectory the directory that the command was invoked from
	 * @param stdout           the standard output stream of the command
	 * @param stderr           the standard error stream of the command
	 * @param exitCode         the exit code returned by the command
	 */
	public CommandResult(List<String> command, Path workingDirectory, String stdout, String stderr,
		int exitCode)
	{
		assert command != null;
		assert stdout != null;
		assert stderr != null;
		this.command = List.copyOf(command);
		this.workingDirectory = workingDirectory;
		this.stdout = stdout;
		this.stderr = stderr;
		this.exitCode = exitCode;
	}

	/**
	 * Returns an AssertionError indicating that the command returned an unexpected response.
	 *
	 * @return an explanation of the failure
	 */
	public AssertionError unexpectedResponse()
	{
		return new AssertionError(command + " returned an unexpected response.\n" +
			"exitCode   : " + exitCode + ".\n" +
			"stdout     : " + stdout + "\n" +
			"stderr     : " + stderr + "\n" +
			"directory  : " + workingDirectory);
	}
}