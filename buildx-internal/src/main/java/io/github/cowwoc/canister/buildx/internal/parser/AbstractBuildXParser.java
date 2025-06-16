package io.github.cowwoc.canister.buildx.internal.parser;

import io.github.cowwoc.canister.buildx.internal.client.InternalBuildXClient;
import io.github.cowwoc.canister.core.internal.parser.AbstractParser;

import java.util.regex.Pattern;

/**
 * Code that is shared by all container parsers.
 */
public abstract class AbstractBuildXParser extends AbstractParser
{
	/**
	 * The network connection has been unexpectedly terminated.
	 */
	public static final Pattern CONNECTION_RESET = Pattern.compile("error during connect: " +
		"[^ ]+ (\"[^\"]+\"): EOF");
	// Known variants:
	// failed to remove context ContainerIT.create: failed to remove metadata: remove C:\Users\Gili\.docker\contexts\meta\3f71ea6b1d70857e99ba7c2f25b1a5103c3ae9688a5485620732f10d0a255381\meta.json: The process cannot access the file because it is being used by another process.
	// ERROR: failed to build: open C:\Users\Gili\.docker\contexts\meta\72510d5f6839cea902e7cb4bb977d0956dd2b4d68066afb70c280e0a6bb30c0f: The process cannot access the file because it is being used by another process.
	public static final Pattern FILE_LOCKED = Pattern.compile("""
		.* (open|remove) (\\S.*?): The process cannot access the file because it is being used by another \
		process\\.""");
	// Known variants:
	// ERROR: failed to build: failed to read metadata: open C:\Users\Gili\.docker\contexts\meta\277f91ae09d6411b85f443e4ec9ae29e281bcf0815e8407f7bab2d88cfb61554\meta.json: Access is denied.
	public static final Pattern ACCESS_DENIED = Pattern.compile(".* open (\\S.*?): Access is denied\\.");

	/**
	 * Creates a AbstractContainerParser.
	 *
	 * @param client the client configuration
	 */
	public AbstractBuildXParser(InternalBuildXClient client)
	{
		super(client);
	}

	@Override
	protected InternalBuildXClient getClient()
	{
		return (InternalBuildXClient) client;
	}
}