package io.github.cowwoc.canister.docker.main.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.cowwoc.canister.core.exception.ResourceInUseException;
import io.github.cowwoc.canister.core.exception.ResourceNotFoundException;
import io.github.cowwoc.canister.core.id.ImageId;
import io.github.cowwoc.canister.core.resource.CommandResult;
import io.github.cowwoc.canister.docker.api.client.DockerClient;
import io.github.cowwoc.canister.docker.main.client.InternalDockerClient;
import io.github.cowwoc.canister.docker.main.resource.DefaultDockerImage;
import io.github.cowwoc.canister.docker.resource.DockerImage;
import io.github.cowwoc.canister.docker.resource.DockerImageElement;
import io.github.cowwoc.canister.docker.resource.ImageRemover;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.that;

/**
 * Parses responses to {@code Image} commands.
 */
public final class ImageParser extends DockerParser
{
	private static final Pattern NOT_FOUND = Pattern.compile(
		"Error response from daemon: No such image: ([^ ]+)");
	private static final Pattern PULL_REPOSITORY_NOT_FOUND = Pattern.compile("""
		Error response from daemon: pull access denied for [^,]+, repository does not exist or may require \
		'docker login'""");
	private static final Pattern PULL_MANIFEST_NOT_FOUND = Pattern.compile(
		"Error response from daemon: manifest for [^ ]+ not found: manifest unknown: manifest unknown");
	private static final Pattern PULL_ACCESS_DENIED1 = Pattern.compile("""
		Error response from daemon: Head "[^"]+": denied""");
	private static final String PULL_ACCESS_DENIED2 =
		"Error response from daemon: error from registry: denied\ndenied";
	private static final Pattern PUSH_NOT_FOUND = Pattern.compile("""
		Error response from daemon: push access denied for ([^,]+), repository does not exist or may require \
		'docker login'""");
	private static final Pattern REMOVE_MUST_BE_FORCED = Pattern.compile(
		"Error response from daemon: conflict: unable to delete ([^ ]+) \\(must be forced\\) -");

	/**
	 * Creates a parser.
	 *
	 * @param client the client configuration
	 */
	public ImageParser(InternalDockerClient client)
	{
		super(client);
	}

	@Override
	protected InternalDockerClient getClient()
	{
		return (InternalDockerClient) super.getClient();
	}

	/**
	 * Lists all the images.
	 *
	 * @param result the result of executing a command
	 * @return an empty list if no match is found
	 */
	public List<DockerImageElement> list(CommandResult result)
	{
		if (result.exitCode() != 0)
			throw result.unexpectedResponse();
		Map<String, Map<String, Set<String>>> idToRepositoryToTags = new HashMap<>();
		Map<String, Map<String, String>> idToRepositoryToDigest = new HashMap<>();
		JsonMapper jm = getClient().getJsonMapper();
		try
		{
			for (String line : SPLIT_LINES.split(result.stdout()))
			{
				if (line.isBlank())
					continue;
				JsonNode json = jm.readTree(line);
				String id = json.get("ID").textValue();
				Map<String, String> repositoryToDigest = idToRepositoryToDigest.computeIfAbsent(id,
					_ -> new HashMap<>());
				Map<String, Set<String>> referenceToTags = idToRepositoryToTags.computeIfAbsent(id,
					_ -> new HashMap<>());

				String reference = json.get("Repository").textValue();
				if (reference.equals("<none>"))
					continue;

				String digest = json.get("Digest").textValue();
				if (!digest.equals("<none>"))
					repositoryToDigest.put(reference, digest);

				String tag = json.get("Tag").textValue();
				if (!tag.equals("<none>"))
					referenceToTags.computeIfAbsent(reference, _ -> new HashSet<>()).add(tag);
			}
			List<DockerImageElement> elements = new ArrayList<>(idToRepositoryToTags.size());
			for (String id : idToRepositoryToTags.keySet())
			{
				Map<String, Set<String>> repositoryToTags = idToRepositoryToTags.get(id);
				Map<String, String> repositoryToDigest = idToRepositoryToDigest.get(id);
				elements.add(new DockerImageElement(ImageId.of(id), repositoryToTags, repositoryToDigest));
			}
			return elements;
		}
		catch (JsonProcessingException e)
		{
			throw new AssertionError(e);
		}
	}

	/**
	 * Looks up an image by its ID or name.
	 *
	 * @param result the result of executing a command
	 * @return null if no match is found
	 */
	public DockerImage imageFromServer(CommandResult result)
	{
		if (result.exitCode() != 0)
		{
			if (NOT_FOUND.matcher(result.stderr()).matches())
				return null;
			throw result.unexpectedResponse();
		}
		JsonMapper jm = getClient().getJsonMapper();
		try
		{
			JsonNode json = jm.readTree(result.stdout());
			assert json.size() == 1 : json;
			return imageFromServer(json.get(0));
		}
		catch (JsonProcessingException e)
		{
			throw new AssertionError(e);
		}
	}

	/**
	 * @param json the JSON representation of the node
	 * @return the image
	 */
	private DockerImage imageFromServer(JsonNode json)
	{
		ImageId id = ImageId.of(json.get("Id").textValue());
		Map<String, Set<String>> referenceToTags = new LinkedHashMap<>();
		for (JsonNode node : json.get("RepoTags"))
		{
			String[] nameAndTag = SPLIT_ON_COLON.split(node.textValue());
			assert that(nameAndTag, "nameAndTag").length().isEqualTo(2).elseThrow();
			String name = nameAndTag[0];
			String tag = nameAndTag[1];
			if (!name.equals("<none>") && !tag.equals("<none>"))
				referenceToTags.computeIfAbsent(name, _ -> new HashSet<>()).add(tag);
		}
		Map<String, String> nameToDigest = new LinkedHashMap<>();
		for (JsonNode node : json.get("RepoDigests"))
		{
			String[] nameAndDigest = SPLIT_ON_AT_SIGN.split(node.textValue());
			assert that(nameAndDigest, "nameAndDigest").length().isEqualTo(2).elseThrow();
			String name = nameAndDigest[0];
			String digest = nameAndDigest[1];

			if (!name.equals("<none>") && !digest.equals("<none>"))
				nameToDigest.put(name, digest);
		}
		return new DefaultDockerImage(getClient(), id, referenceToTags, nameToDigest);
	}

	/**
	 * Tags this image with a new name.
	 * <p>
	 * If the name already exists, this method has no effect.
	 *
	 * @param result the result of executing a command
	 * @throws ResourceNotFoundException if the image no longer exists
	 */
	public void tag(CommandResult result) throws ResourceNotFoundException
	{
		if (result.exitCode() != 0)
		{
			Matcher matcher = NOT_FOUND.matcher(result.stderr());
			if (matcher.matches())
				throw new ResourceNotFoundException("Image not found: " + matcher.group(1));
			throw result.unexpectedResponse();
		}
	}

	/**
	 * Pulls the image from a registry.
	 *
	 * @param result    the result of executing a command
	 * @param reference the image reference
	 * @return the ID of the pulled image
	 * @throws ResourceNotFoundException if the image does not exist or may require {@code docker login}
	 * @see DockerClient#login(String, String, String)
	 */
	public ImageId pull(CommandResult result, String reference) throws ResourceNotFoundException
	{
		if (result.exitCode() != 0)
		{
			String stderr = result.stderr();
			Matcher matcher = PULL_REPOSITORY_NOT_FOUND.matcher(stderr);
			if (!matcher.matches())
				matcher = PULL_MANIFEST_NOT_FOUND.matcher(stderr);
			if (!matcher.matches())
				matcher = PULL_ACCESS_DENIED1.matcher(stderr);
			if (matcher.matches())
				throw new ResourceNotFoundException("Image not found or may require \"docker login\": " + reference);
			if (stderr.equals(PULL_ACCESS_DENIED2))
				throw new ResourceNotFoundException("Image not found or may require \"docker login\": " + reference);
			throw result.unexpectedResponse();
		}
		String digest = null;
		for (String line : SPLIT_LINES.split(result.stdout()))
		{
			if (line.isBlank())
				continue;
			if (line.startsWith("Digest:"))
				digest = line.substring("Digest:".length()).strip();
		}
		if (digest == null)
			throw result.unexpectedResponse();
		return ImageId.of(digest);
	}

	/**
	 * Pushes the image to a registry.
	 *
	 * @param result the result of executing a command
	 * @throws ResourceNotFoundException if the referenced image could not be found
	 * @see DockerClient#login(String, String, String)
	 */
	public void push(CommandResult result) throws ResourceNotFoundException
	{
		if (result.exitCode() != 0)
		{
			String stderr = result.stderr();
			Matcher matcher = PUSH_NOT_FOUND.matcher(stderr);
			if (matcher.matches())
				throw new ResourceNotFoundException("Image not found: " + matcher.group(1));
			throw result.unexpectedResponse();
		}
	}

	/**
	 * Removes the image. If the image does not exist, this method has no effect.
	 *
	 * @param result the result of executing a command
	 * @throws ResourceInUseException if the image is tagged in multiple repositories or in use by containers
	 *                                and {@link ImageRemover#force()} was not used
	 */
	public void remove(CommandResult result) throws ResourceInUseException
	{
		if (result.exitCode() != 0)
		{
			String stderr = result.stderr();
			Matcher matcher = NOT_FOUND.matcher(stderr);
			if (matcher.matches())
				return;
			matcher = REMOVE_MUST_BE_FORCED.matcher(stderr);
			if (matcher.find())
			{
				String message = stderr.substring("Error response from daemon: conflict: ".length());
				message = Character.toUpperCase(message.charAt(0)) + message.substring(1);
				throw new ResourceInUseException(message);
			}
			throw result.unexpectedResponse();
		}
	}
}