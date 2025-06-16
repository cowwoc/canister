package io.github.cowwoc.canister.docker.main.parser;

import io.github.cowwoc.canister.core.client.Client;
import io.github.cowwoc.canister.core.internal.parser.AbstractParser;

import java.util.regex.Pattern;

public class DockerParser extends AbstractParser
{
	/**
	 * Splits Strings on a {@code :}.
	 */
	protected static final Pattern SPLIT_ON_COLON = Pattern.compile(":");
	/**
	 * Splits Strings on a {@code @}.
	 */
	protected static final Pattern SPLIT_ON_AT_SIGN = Pattern.compile("@");
	/**
	 * Splits Strings on a {@code /}.
	 */
	protected static final Pattern SPLIT_ON_SLASH = Pattern.compile("/");

	/**
	 * Creates a new DockerParser.
	 *
	 * @param client the client configuration
	 */
	protected DockerParser(Client client)
	{
		super(client);
	}
}
