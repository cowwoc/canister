package io.github.cowwoc.canister.core.exception;

import java.io.Serial;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Thrown if the build driver does not support one of the requested exporters.
 */
public class UnsupportedExporterException extends IllegalStateException
{
	@Serial
	private static final long serialVersionUID = 0L;

	/**
	 * Creates an exception.
	 *
	 * @param message an explanation of what went wrong
	 */
	public UnsupportedExporterException(String message)
	{
		super(message);
	}

	/**
	 * Creates an exception.
	 *
	 * @param cause the underlying exception
	 * @throws NullPointerException if {@code cause} is null
	 */
	public UnsupportedExporterException(Throwable cause)
	{
		super(cause);
		requireThat(cause, "cause").isNotNull();
	}
}