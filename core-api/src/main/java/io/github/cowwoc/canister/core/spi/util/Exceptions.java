package io.github.cowwoc.canister.core.spi.util;

import java.io.IOException;
import java.util.Collection;

/**
 * Helper functions for exceptions.
 */
public final class Exceptions
{
	private Exceptions()
	{
	}

	/**
	 * Combines multiple exceptions into an {@code IOException}.
	 *
	 * @param throwables zero or more exceptions
	 * @return null if no errors occurred
	 */
	public static IOException combineAsIOException(Collection<Throwable> throwables)
	{
		if (throwables.isEmpty())
			return null;
		if (throwables.size() == 1)
		{
			Throwable first = throwables.iterator().next();
			if (first instanceof IOException ioe)
				return ioe;
			return new IOException(first);
		}
		StringBuilder combinedMessage = new StringBuilder(38).append("The operation threw ").
			append(throwables.size()).append(" exceptions.\n");
		int i = 1;
		for (Throwable exception : throwables)
		{
			combinedMessage.append(i).append(". ").append(exception.getClass().getName());
			String message = exception.getMessage();
			if (message != null)
			{
				combinedMessage.append(": ").
					append(message).
					append('\n');
			}
			++i;
		}
		return new IOException(combinedMessage.toString());
	}
}