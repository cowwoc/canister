package io.github.cowwoc.canister.core.spi.util;

import java.util.regex.Pattern;

/**
 * Response parsing shared by the buildx implementation.
 */
public final class Responses
{
	// Known variants:
	// ERROR: failed to build: no builder ("[^"]+") found
	// ERROR: no builder "ImageIT.buildAndOutputDockerImageToTarFile" found
	public static final Pattern NOT_FOUND = Pattern.compile(
		"ERROR: (?:failed to build: )?no builder (\"[^\"]+\") found");
}