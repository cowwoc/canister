package io.github.cowwoc.canister.docker.api.client;

import io.github.cowwoc.canister.buildx.api.client.BuildXClient;
import io.github.cowwoc.canister.core.exception.ResourceInUseException;
import io.github.cowwoc.canister.core.exception.ResourceNotFoundException;
import io.github.cowwoc.canister.core.id.ImageId;
import io.github.cowwoc.canister.core.resource.Image;
import io.github.cowwoc.canister.docker.exception.NotSwarmManagerException;
import io.github.cowwoc.canister.docker.exception.NotSwarmMemberException;
import io.github.cowwoc.canister.docker.id.ConfigId;
import io.github.cowwoc.canister.docker.id.ContainerId;
import io.github.cowwoc.canister.docker.id.ContextId;
import io.github.cowwoc.canister.docker.id.NetworkId;
import io.github.cowwoc.canister.docker.id.NodeId;
import io.github.cowwoc.canister.docker.id.ServiceId;
import io.github.cowwoc.canister.docker.id.TaskId;
import io.github.cowwoc.canister.docker.resource.Config;
import io.github.cowwoc.canister.docker.resource.ConfigCreator;
import io.github.cowwoc.canister.docker.resource.ConfigElement;
import io.github.cowwoc.canister.docker.resource.Container;
import io.github.cowwoc.canister.docker.resource.ContainerCreator;
import io.github.cowwoc.canister.docker.resource.ContainerElement;
import io.github.cowwoc.canister.docker.resource.ContainerLogs;
import io.github.cowwoc.canister.docker.resource.ContainerRemover;
import io.github.cowwoc.canister.docker.resource.ContainerStarter;
import io.github.cowwoc.canister.docker.resource.ContainerStopper;
import io.github.cowwoc.canister.docker.resource.Context;
import io.github.cowwoc.canister.docker.resource.ContextCreator;
import io.github.cowwoc.canister.docker.resource.ContextElement;
import io.github.cowwoc.canister.docker.resource.ContextEndpoint;
import io.github.cowwoc.canister.docker.resource.ContextRemover;
import io.github.cowwoc.canister.docker.resource.DockerImage;
import io.github.cowwoc.canister.docker.resource.DockerImageBuilder;
import io.github.cowwoc.canister.docker.resource.DockerImageElement;
import io.github.cowwoc.canister.docker.resource.ImagePuller;
import io.github.cowwoc.canister.docker.resource.ImagePusher;
import io.github.cowwoc.canister.docker.resource.ImageRemover;
import io.github.cowwoc.canister.docker.resource.JoinToken;
import io.github.cowwoc.canister.docker.resource.Network;
import io.github.cowwoc.canister.docker.resource.NetworkElement;
import io.github.cowwoc.canister.docker.resource.Node;
import io.github.cowwoc.canister.docker.resource.Node.Role;
import io.github.cowwoc.canister.docker.resource.NodeElement;
import io.github.cowwoc.canister.docker.resource.NodeRemover;
import io.github.cowwoc.canister.docker.resource.Service;
import io.github.cowwoc.canister.docker.resource.ServiceCreator;
import io.github.cowwoc.canister.docker.resource.ServiceElement;
import io.github.cowwoc.canister.docker.resource.SwarmCreator;
import io.github.cowwoc.canister.docker.resource.SwarmJoiner;
import io.github.cowwoc.canister.docker.resource.SwarmLeaver;
import io.github.cowwoc.canister.docker.resource.Task;
import io.github.cowwoc.requirements12.annotation.CheckReturnValue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

/**
 * A Docker client.
 */
public interface DockerClient extends BuildXClient
{
	@Override
	DockerClient retryTimeout(Duration duration);

	/**
	 * Authenticates with the Docker Hub registry.
	 *
	 * @param username the user's name
	 * @param password the user's password
	 * @return this
	 * @throws NullPointerException     if any of the mandatory parameters are null
	 * @throws IllegalArgumentException if:
	 *                                  <ul>
	 *                                    <li>any of the arguments contain whitespace.</li>
	 *                                    <li>{@code username} or {@code password} is empty.</li>
	 *                                  </ul>
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	DockerClient login(String username, String password) throws IOException, InterruptedException;

	/**
	 * Authenticates with a registry.
	 *
	 * @param username      the user's name
	 * @param password      the user's password
	 * @param serverAddress the name of a registry server
	 * @return this
	 * @throws NullPointerException     if any of the mandatory parameters are null
	 * @throws IllegalArgumentException if:
	 *                                  <ul>
	 *                                    <li>any of the arguments contain whitespace.</li>
	 *                                    <li>any of the arguments are empty.</li>
	 *                                  </ul>
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	DockerClient login(String username, String password, String serverAddress)
		throws IOException, InterruptedException;

	/**
	 * Returns all the configs.
	 *
	 * @return an empty list if no match is found
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	List<Config> getConfigs() throws IOException, InterruptedException;

	/**
	 * Returns the configs that match a predicate.
	 *
	 * @param predicate the predicate
	 * @return an empty list if no match is found
	 * @throws NullPointerException     if {@code predicate} is null
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	List<Config> getConfigs(Predicate<ConfigElement> predicate) throws IOException, InterruptedException;

	/**
	 * Returns a config.
	 *
	 * @param id the config's ID or name
	 * @return null if no match is found
	 * @throws NullPointerException     if {@code id} is null
	 * @throws IllegalArgumentException if {@code id}'s format is invalid
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	Config getConfig(String id) throws IOException, InterruptedException;

	/**
	 * Returns a config.
	 *
	 * @param id the config's ID or name
	 * @return null if no match is found
	 * @throws NullPointerException     if {@code id} is null
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	Config getConfig(ConfigId id) throws IOException, InterruptedException;

	/**
	 * Creates a config.
	 *
	 * @return a config creator
	 */
	@CheckReturnValue
	ConfigCreator createConfig();

	/**
	 * Returns all the containers.
	 *
	 * @return an empty list if no match is found
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	List<Container> getContainers() throws IOException, InterruptedException;

	/**
	 * Returns the containers that match a predicate.
	 *
	 * @param predicate the predicate
	 * @return an empty list if no match is found
	 * @throws NullPointerException if {@code predicate} is null
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	List<Container> getContainers(Predicate<ContainerElement> predicate)
		throws IOException, InterruptedException;

	/**
	 * Returns a container.
	 *
	 * @param id the container's ID or name
	 * @return the container
	 * @throws NullPointerException     if {@code id} is null
	 * @throws IllegalArgumentException if {@code id}'s format is invalid
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	Container getContainer(String id) throws IOException, InterruptedException;

	/**
	 * Returns a container.
	 *
	 * @param id the container's ID or name
	 * @return the container
	 * @throws NullPointerException if {@code id} is null
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	Container getContainer(ContainerId id) throws IOException, InterruptedException;

	/**
	 * Creates a container.
	 *
	 * @param imageId the image ID or {@link Image reference} to create the container from
	 * @return a container creator
	 * @throws NullPointerException     if {@code imageId} is null
	 * @throws IllegalArgumentException if {@code imageId}'s format is invalid
	 */
	@CheckReturnValue
	ContainerCreator createContainer(String imageId);

	/**
	 * Creates a container.
	 *
	 * @param imageId the image ID or {@link Image reference} to create the container from
	 * @return a container creator
	 * @throws NullPointerException if {@code imageId} is null
	 */
	@CheckReturnValue
	ContainerCreator createContainer(ImageId imageId);

	/**
	 * Renames a container.
	 *
	 * @param id      the ID of the container to rename
	 * @param newName the container's new name
	 * @return this
	 * @throws NullPointerException      if any of the arguments are null
	 * @throws IllegalArgumentException  if:
	 *                                   <ul>
	 *                                     <li>{@code id}'s format is invalid.</li>
	 *                                     <li>{@code newName} is empty.</li>
	 *                                     <li>{@code newName} contains any character other than lowercase
	 *                                     letters (a–z), digits (0–9), and the following characters:
	 *                                     {@code '.'}, {@code '/'}, {@code ':'}, {@code '_'}, {@code '-'},
	 *                                     {@code '@'}.</li>
	 *                                   </ul>
	 * @throws ResourceNotFoundException if the container does not exist
	 * @throws ResourceInUseException    if the requested name is in use by another container
	 * @throws IOException               if an I/O error occurs. These errors are typically transient, and
	 *                                   retrying the request may resolve the issue.
	 * @throws InterruptedException      if the thread is interrupted before the operation completes. This can
	 *                                   happen due to shutdown signals.
	 */
	DockerClient renameContainer(String id, String newName) throws IOException, InterruptedException;

	/**
	 * Renames a container.
	 *
	 * @param id      the ID of the container to rename
	 * @param newName the container's new name
	 * @return this
	 * @throws NullPointerException      if any of the arguments are null
	 * @throws IllegalArgumentException  if {@code newName}:
	 *                                   <ul>
	 *                                     <li>is empty.</li>
	 *                                     <li>contains any character other than lowercase letters (a–z),
	 *                                     digits (0–9), and the following characters: {@code '.'}, {@code '/'},
	 *                                     {@code ':'}, {@code '_'}, {@code '-'}, {@code '@'}.</li>
	 *                                   </ul>
	 * @throws ResourceNotFoundException if the container does not exist
	 * @throws ResourceInUseException    if the requested name is in use by another container
	 * @throws IOException               if an I/O error occurs. These errors are typically transient, and
	 *                                   retrying the request may resolve the issue.
	 * @throws InterruptedException      if the thread is interrupted before the operation completes. This can
	 *                                   happen due to shutdown signals.
	 */
	DockerClient renameContainer(ContainerId id, String newName)
		throws IOException, InterruptedException;

	/**
	 * Starts a container.
	 *
	 * @param id the container's ID or name
	 * @return a container starter
	 * @throws NullPointerException     if {@code id} is null
	 * @throws IllegalArgumentException if {@code id}'s format is invalid
	 */
	@CheckReturnValue
	ContainerStarter startContainer(String id);

	/**
	 * Starts a container.
	 *
	 * @param id the container's ID or name
	 * @return a container starter
	 * @throws NullPointerException if {@code id} is null
	 */
	@CheckReturnValue
	ContainerStarter startContainer(ContainerId id);

	/**
	 * Stops a container.
	 *
	 * @param id the container's ID or name
	 * @return a container stopper
	 * @throws NullPointerException     if {@code id} is null
	 * @throws IllegalArgumentException if {@code id}'s format is invalid
	 */
	@CheckReturnValue
	ContainerStopper stopContainer(String id);

	/**
	 * Stops a container.
	 *
	 * @param id the container's ID or name
	 * @return a container stopper
	 * @throws NullPointerException if {@code id} is null
	 */
	@CheckReturnValue
	ContainerStopper stopContainer(ContainerId id);

	/**
	 * Removes a container.
	 *
	 * @param id the container's ID or name
	 * @return a container remover
	 * @throws NullPointerException     if {@code id} is null
	 * @throws IllegalArgumentException if {@code id}'s format is invalid
	 */
	@CheckReturnValue
	ContainerRemover removeContainer(String id);

	/**
	 * Removes a container.
	 *
	 * @param id the container's ID or name
	 * @return a container remover
	 * @throws NullPointerException if {@code id} is null
	 */
	@CheckReturnValue
	ContainerRemover removeContainer(ContainerId id);

	/**
	 * Waits until a container stops.
	 * <p>
	 * If the container has already stopped, this method returns immediately.
	 *
	 * @param id the container's ID or name
	 * @return the exit code returned by the container
	 * @throws NullPointerException      if {@code id} is null
	 * @throws IllegalArgumentException  if {@code id}'s format is invalid
	 * @throws ResourceNotFoundException if the container does not exist
	 * @throws IOException               if an I/O error occurs. These errors are typically transient, and
	 *                                   retrying the request may resolve the issue.
	 * @throws InterruptedException      if the thread is interrupted before the operation completes. This can
	 *                                   happen due to shutdown signals.
	 */
	int waitUntilContainerStops(String id) throws IOException, InterruptedException;

	/**
	 * Waits until a container stops.
	 * <p>
	 * If the container has already stopped, this method returns immediately.
	 *
	 * @param id the container's ID or name
	 * @return the exit code returned by the container
	 * @throws NullPointerException      if {@code id} is null
	 * @throws ResourceNotFoundException if the container does not exist
	 * @throws IOException               if an I/O error occurs. These errors are typically transient, and
	 *                                   retrying the request may resolve the issue.
	 * @throws InterruptedException      if the thread is interrupted before the operation completes. This can
	 *                                   happen due to shutdown signals.
	 */
	int waitUntilContainerStops(ContainerId id) throws IOException, InterruptedException;

	/**
	 * Waits until a container has the desired status.
	 * <p>
	 * If the container already has the desired status, this method returns immediately.
	 *
	 * @param id the container's ID or name
	 * @throws NullPointerException      if any of the arguments are null
	 * @throws IllegalArgumentException  if {@code id}'s format is invalid
	 * @throws ResourceNotFoundException if the container does not exist
	 * @throws IOException               if an I/O error occurs. These errors are typically transient, and
	 *                                   retrying the request may resolve the issue.
	 * @throws InterruptedException      if the thread is interrupted before the operation completes. This can
	 *                                   happen due to shutdown signals.
	 */
	void waitUntilContainerStatus(Container.Status status, String id) throws IOException, InterruptedException;

	/**
	 * Waits until a container has the desired status.
	 * <p>
	 * If the container already has the desired status, this method returns immediately.
	 *
	 * @param status the desired status
	 * @param id     the container's ID or name
	 * @return the updated container
	 * @throws NullPointerException      if any of the arguments are null
	 * @throws ResourceNotFoundException if the container does not exist
	 * @throws IOException               if an I/O error occurs. These errors are typically transient, and
	 *                                   retrying the request may resolve the issue.
	 * @throws InterruptedException      if the thread is interrupted before the operation completes. This can
	 *                                   happen due to shutdown signals.
	 */
	Container waitUntilContainerStatus(Container.Status status, ContainerId id)
		throws IOException, InterruptedException;

	/**
	 * Retrieves a container's logs.
	 *
	 * @param id the container's ID or name
	 * @return the logs
	 * @throws NullPointerException     if {@code id} is null
	 * @throws IllegalArgumentException if {@code id}'s format is invalid
	 */
	ContainerLogs getContainerLogs(String id);

	/**
	 * Retrieves a container's logs.
	 *
	 * @param id the container's ID or name
	 * @return the logs
	 * @throws NullPointerException if {@code id} is null
	 */
	ContainerLogs getContainerLogs(ContainerId id);

	/**
	 * Returns all the contexts.
	 *
	 * @return an empty list if no match is found
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	List<Context> getContexts() throws IOException, InterruptedException;

	/**
	 * Returns the contexts that match a predicate.
	 *
	 * @param predicate the predicate
	 * @return an empty list if no match is found
	 * @throws NullPointerException if {@code predicate} is null
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	List<Context> getContexts(Predicate<ContextElement> predicate)
		throws IOException, InterruptedException;

	/**
	 * Returns a context.
	 *
	 * @param id the context's ID
	 * @return the context
	 * @throws NullPointerException     if {@code id} is null
	 * @throws IllegalArgumentException if {@code id}'s format is invalid
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	Context getContext(String id) throws IOException, InterruptedException;

	/**
	 * Returns a context.
	 *
	 * @param id the context's ID
	 * @return the context
	 * @throws NullPointerException if {@code id} is null
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	Context getContext(ContextId id) throws IOException, InterruptedException;

	/**
	 * Creates a context.
	 *
	 * @param name     the name of the context
	 * @param endpoint the configuration of the target Docker Engine
	 * @return a context creator
	 * @throws NullPointerException     if any of the arguments are null
	 * @throws IllegalArgumentException if {@code name} contains whitespace or is empty
	 * @see ContextEndpoint#builder(URI)
	 */
	@CheckReturnValue
	ContextCreator createContext(String name, ContextEndpoint endpoint);

	/**
	 * Removes an existing context.
	 *
	 * @param name the name of the context
	 * @return the context remover
	 * @throws NullPointerException     if {@code name} is null
	 * @throws IllegalArgumentException if {@code name}'s format is invalid
	 */
	@CheckReturnValue
	ContextRemover removeContext(String name);

	/**
	 * Removes an existing context.
	 *
	 * @param id the context's ID
	 * @return the context remover
	 * @throws NullPointerException if {@code id} is null
	 */
	@CheckReturnValue
	ContextRemover removeContext(ContextId id);

	/**
	 * Returns the client's current context.
	 *
	 * @return {@code null} if the client is using the user's context
	 * @see <a href="https://docs.docker.com/engine/security/protect-access/">Protect the Docker daemon
	 * 	socket</a>
	 * @see <a href="https://docs.docker.com/engine/manage-resources/contexts/">global --context flag</a>
	 * @see #getUserContext()
	 */
	ContextId getClientContext();

	/**
	 * Sets the client's current context. Unlike {@link #setUserContext(ContextId)}, this method only updates
	 * the current client's configuration and does not affect other processes or shells.
	 *
	 * @param id the context's ID, or an empty string to use the user's context
	 * @return this
	 * @throws IllegalArgumentException if {@code id}'s format is invalid
	 */
	DockerClient setClientContext(String id);

	/**
	 * Sets the client's current context. Unlike {@link #setUserContext(ContextId)}, this method only updates
	 * the current client's configuration and does not affect other processes or shells.
	 *
	 * @param id the context's ID, or null to use the user's context
	 * @return this
	 */
	DockerClient setClientContext(ContextId id);

	/**
	 * Returns the current user's context.
	 *
	 * @return null if the default context is used
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 * @see <a href="https://docs.docker.com/engine/security/protect-access/">Protect the Docker daemon
	 * 	socket</a>
	 * @see <a href="https://docs.docker.com/reference/cli/docker/context/use/">docker context use</a>
	 * @see #getClientContext()
	 */
	ContextId getUserContext() throws IOException, InterruptedException;

	/**
	 * Sets the current user's current context. Unlike {@link #setClientContext(ContextId)}, this method updates
	 * the persistent Docker CLI configuration and affects all future Docker CLI invocations by the user across
	 * all shells.
	 *
	 * @param id the context's ID
	 * @return this
	 * @throws NullPointerException      if {@code id} is null
	 * @throws IllegalArgumentException  if {@code id}'s format is invalid
	 * @throws ResourceNotFoundException if the context does not exist
	 * @throws IOException               if an I/O error occurs. These errors are typically transient, and
	 *                                   retrying the request may resolve the issue.
	 * @throws InterruptedException      if the thread is interrupted before the operation completes. This can
	 *                                   happen due to shutdown signals.
	 */
	DockerClient setUserContext(String id) throws IOException, InterruptedException;

	/**
	 * Sets the current user's current context. Unlike {@link #setClientContext(ContextId)}, this method updates
	 * the persistent Docker CLI configuration and affects all future Docker CLI invocations by the user across
	 * all shells.
	 *
	 * @param id the context's ID
	 * @return this
	 * @throws NullPointerException      if {@code id} is null
	 * @throws ResourceNotFoundException if the context does not exist
	 * @throws IOException               if an I/O error occurs. These errors are typically transient, and
	 *                                   retrying the request may resolve the issue.
	 * @throws InterruptedException      if the thread is interrupted before the operation completes. This can
	 *                                   happen due to shutdown signals.
	 */
	DockerClient setUserContext(ContextId id) throws IOException, InterruptedException;

	/**
	 * Returns all the image IDs.
	 *
	 * @return an empty list if no match is found
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	List<ImageId> getImageIds() throws IOException, InterruptedException;

	/**
	 * Returns the image IDs that match a predicate.
	 *
	 * @param predicate the predicate
	 * @return an empty list if no match is found
	 * @throws NullPointerException if {@code predicate} is null
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	List<ImageId> getImageIds(Predicate<DockerImageElement> predicate) throws IOException, InterruptedException;

	/**
	 * Returns all the images.
	 *
	 * @return an empty list if no match is found
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	List<DockerImage> getImages() throws IOException, InterruptedException;

	/**
	 * Returns the images that match a predicate.
	 *
	 * @param predicate the predicate
	 * @return an empty list if no match is found
	 * @throws NullPointerException if {@code predicate} is null
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	List<DockerImage> getImages(Predicate<DockerImageElement> predicate)
		throws IOException, InterruptedException;

	/**
	 * Looks up an image.
	 *
	 * @param id the image's ID or {@link Image reference}
	 * @return the image
	 * @throws NullPointerException     if {@code id} is null
	 * @throws IllegalArgumentException if {@code id}'s format is invalid
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	DockerImage getImage(String id) throws IOException, InterruptedException;

	/**
	 * Looks up an image.
	 *
	 * @param id the image's ID or {@link Image reference}
	 * @return the image
	 * @throws NullPointerException if {@code id} is null
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	DockerImage getImage(ImageId id) throws IOException, InterruptedException;

	/**
	 * Builds an image.
	 *
	 * @return an image builder
	 */
	@Override
	@CheckReturnValue
	DockerImageBuilder buildImage();

	/**
	 * Adds a new tag to an existing image, creating an additional reference without duplicating image data.
	 * <p>
	 * If the target reference already exists, this method has no effect.
	 *
	 * @param id     the ID or existing {@link Image reference} of the image
	 * @param target the new reference to create
	 * @throws NullPointerException      if any of the arguments are null
	 * @throws IllegalArgumentException  if {@code id} or {@code target}'s format are invalid
	 * @throws ResourceNotFoundException if the image does not exist
	 * @throws IOException               if an I/O error occurs. These errors are typically transient, and
	 *                                   retrying the request may resolve the issue.
	 * @throws InterruptedException      if the thread is interrupted before the operation completes. This can
	 *                                   happen due to shutdown signals.
	 */
	void tagImage(String id, String target) throws IOException, InterruptedException;

	/**
	 * Adds a new tag to an existing image, creating an additional reference without duplicating image data.
	 * <p>
	 * If the target reference already exists, this method has no effect.
	 *
	 * @param id     the ID or existing {@link Image reference} of the image
	 * @param target the new reference to create
	 * @throws NullPointerException      if any of the arguments are null
	 * @throws IllegalArgumentException  if {@code target}'s format is invalid
	 * @throws ResourceNotFoundException if the image does not exist
	 * @throws IOException               if an I/O error occurs. These errors are typically transient, and
	 *                                   retrying the request may resolve the issue.
	 * @throws InterruptedException      if the thread is interrupted before the operation completes. This can
	 *                                   happen due to shutdown signals.
	 */
	void tagImage(ImageId id, String target) throws IOException, InterruptedException;

	/**
	 * Pulls an image from a registry.
	 *
	 * @param reference the {@link Image reference} to pull. For example, {@code docker.io/nasa/rocket-ship}
	 * @return an image puller
	 * @throws NullPointerException     if {@code reference} is null
	 * @throws IllegalArgumentException if {@code reference}'s format is invalid
	 */
	@CheckReturnValue
	ImagePuller pullImage(String reference);

	/**
	 * Pushes an image to a registry.
	 *
	 * @param reference the {@link Image reference} to push. For example, {@code docker.io/nasa/rocket-ship}.
	 *                  The image must be present in the local image store with the same name.
	 * @return an image pusher
	 * @throws NullPointerException     if {@code reference} is null
	 * @throws IllegalArgumentException if {@code reference}'s format is invalid
	 */
	@CheckReturnValue
	ImagePusher pushImage(String reference) throws IOException, InterruptedException;

	/**
	 * Removes an image's tag. If the tag is the only one for the image, both the image and the tag are
	 * removed.
	 *
	 * @param reference the reference to remove
	 * @return an image remover
	 * @throws NullPointerException     if {@code reference} is null
	 * @throws IllegalArgumentException if {@link Image reference}'s format is invalid
	 */
	@CheckReturnValue
	ImageRemover removeImageTag(String reference);

	/**
	 * Removes an image and all of its tags.
	 *
	 * @return an image remover
	 * @throws NullPointerException     if {@code id} is null
	 * @throws IllegalArgumentException if {@code id}'s format is invalid
	 */
	@CheckReturnValue
	ImageRemover removeImage(String id);

	/**
	 * Removes an image and all of its tags.
	 *
	 * @return an image remover
	 * @throws NullPointerException if {@code id} is null
	 */
	@CheckReturnValue
	ImageRemover removeImage(ImageId id);

	/**
	 * Creates a swarm.
	 *
	 * @return a swarm creator
	 */
	@CheckReturnValue
	SwarmCreator createSwarm();

	/**
	 * Joins an existing swarm.
	 *
	 * @return a swarm joiner
	 */
	@CheckReturnValue
	SwarmJoiner joinSwarm();

	/**
	 * Leaves a swarm.
	 *
	 * @return a swarm leaver
	 */
	@CheckReturnValue
	SwarmLeaver leaveSwarm();

	/**
	 * Returns the secret value needed to join the swarm as a manager.
	 *
	 * @return the join token
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	JoinToken getManagerJoinToken() throws IOException, InterruptedException;

	/**
	 * Returns the secret value needed to join the swarm as a worker.
	 *
	 * @return the join token
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	JoinToken getWorkerJoinToken() throws IOException, InterruptedException;

	/**
	 * Returns all the networks.
	 *
	 * @return an empty list if no match is found
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	List<Network> getNetworks() throws IOException, InterruptedException;

	/**
	 * Returns the networks that match a predicate.
	 *
	 * @param predicate the predicate
	 * @return an empty list if no match is found
	 * @throws NullPointerException if {@code predicate} is null
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	List<Network> getNetworks(Predicate<NetworkElement> predicate) throws IOException, InterruptedException;

	/**
	 * Looks up a network.
	 *
	 * @param id the network's ID
	 * @return null if no match is found
	 * @throws NullPointerException     if any of the arguments are null
	 * @throws IllegalArgumentException if {@code id}'s format is invalid
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	Network getNetwork(String id) throws IOException, InterruptedException;

	/**
	 * Looks up a network.
	 *
	 * @param id the network's ID
	 * @return null if no match is found
	 * @throws NullPointerException if any of the arguments are null
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	Network getNetwork(NetworkId id) throws IOException, InterruptedException;

	/**
	 * Returns all the swarm nodes.
	 *
	 * @return the nodes
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	List<Node> getNodes() throws IOException, InterruptedException;

	/**
	 * Returns the swarm nodes that match a predicate.
	 *
	 * @param predicate the predicate
	 * @return an empty list if no match is found
	 * @throws NullPointerException     if {@code predicate} is null
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	List<Node> getNodes(Predicate<NodeElement> predicate) throws IOException, InterruptedException;

	/**
	 * Lists the manager nodes in the swarm.
	 *
	 * @return the manager nodes
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	List<NodeElement> listManagerNodes() throws IOException, InterruptedException;

	/**
	 * Lists the worker nodes in the swarm.
	 *
	 * @return the worker nodes
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	List<NodeElement> listWorkerNodes() throws IOException, InterruptedException;

	/**
	 * Looks up the current node's ID.
	 *
	 * @return the ID
	 * @throws NotSwarmMemberException if the current node is not a member of a swarm
	 * @throws IOException             if an I/O error occurs. These errors are typically transient, and
	 *                                 retrying the request may resolve the issue.
	 * @throws InterruptedException    if the thread is interrupted before the operation completes. This can
	 *                                 happen due to shutdown signals.
	 */
	NodeId getCurrentNodeId() throws IOException, InterruptedException;

	/**
	 * Looks up the current node.
	 *
	 * @return the node
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws FileNotFoundException    if the node's unix socket endpoint does not exist
	 * @throws ConnectException         if the node's TCP/IP socket refused a connection
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	Node getCurrentNode() throws IOException, InterruptedException;

	/**
	 * Looks up a node.
	 *
	 * @param id the node's ID or hostname
	 * @return the node
	 * @throws NullPointerException     if {@code id} is null
	 * @throws IllegalArgumentException if {@code id}'s format is invalid
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws FileNotFoundException    if the node's unix socket endpoint does not exist
	 * @throws ConnectException         if the node's TCP/IP socket refused a connection
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	Node getNode(String id) throws IOException, InterruptedException;

	/**
	 * Looks up a node.
	 *
	 * @param id the node's ID or hostname
	 * @return the node
	 * @throws NullPointerException     if {@code id} is null
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws FileNotFoundException    if the node's unix socket endpoint does not exist
	 * @throws ConnectException         if the node's TCP/IP socket refused a connection
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	Node getNode(NodeId id) throws IOException, InterruptedException;

	/**
	 * Lists the tasks that are assigned to the current node.
	 * <p>
	 * This includes tasks in active lifecycle states such as {@code New}, {@code Allocated}, {@code Pending},
	 * {@code Assigned}, {@code Accepted}, {@code Preparing}, {@code Ready}, {@code Starting}, and
	 * {@code Running}. These states represent tasks that are in progress or actively running and are reliably
	 * returned by this command.
	 * <p>
	 * However, tasks that have reached a terminal state—such as {@code Complete}, {@code Failed}, or
	 * {@code Shutdown}— are often pruned by Docker shortly after they exit, and are therefore not guaranteed to
	 * appear in the results, even if they completed very recently.
	 * <p>
	 * Note that Docker prunes old tasks aggressively from this command, so
	 * {@link #getTasksByService(ServiceId)} will often provide more comprehensive historical data by design.
	 *
	 * @return the tasks
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	List<Task> getTasksByNode() throws IOException, InterruptedException;

	/**
	 * Lists the tasks that are assigned to a node.
	 * <p>
	 * Note that Docker prunes old tasks aggressively from this command, so
	 * {@link #getTasksByService(ServiceId)} will often provide more comprehensive historical data by design.
	 *
	 * @param id the node's ID or hostname
	 * @return the tasks
	 * @throws NullPointerException     if {@code id} is null
	 * @throws IllegalArgumentException if {@code id}'s format is invalid
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	List<Task> getTasksByNode(String id) throws IOException, InterruptedException;

	/**
	 * Lists the tasks that are assigned to a node.
	 * <p>
	 * Note that Docker prunes old tasks aggressively from this command, so
	 * {@link #getTasksByService(ServiceId)} will often provide more comprehensive historical data by design.
	 *
	 * @param id the node's ID or hostname
	 * @return the tasks
	 * @throws NullPointerException     if {@code id} is null
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	List<Task> getTasksByNode(NodeId id) throws IOException, InterruptedException;

	/**
	 * Begins gracefully removing tasks from this node and redistribute them to other active nodes.
	 *
	 * @param id the node's ID or hostname
	 * @return the node's updated ID
	 * @throws NullPointerException     if {@code id} is null
	 * @throws IllegalArgumentException if {@code id}'s format is invalid
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	NodeId drainNode(String id) throws IOException, InterruptedException;

	/**
	 * Begins gracefully removing tasks from this node and redistribute them to other active nodes.
	 *
	 * @param id the node's ID or hostname
	 * @return the node's updated ID
	 * @throws NullPointerException if {@code id} is null
	 * @throws IOException          if an I/O error occurs. These errors are typically transient, and retrying
	 *                              the request may resolve the issue.
	 * @throws InterruptedException if the thread is interrupted before the operation completes. This can happen
	 *                              due to shutdown signals.
	 */
	NodeId drainNode(NodeId id) throws IOException, InterruptedException;

	/**
	 * Sets the role of a node.
	 *
	 * @param id       the node's ID or hostname
	 * @param role     the new role
	 * @param deadline the absolute time by which the type must change. The method will poll the node's state
	 *                 while the current time is before this value.
	 * @return the node's updated ID
	 * @throws NullPointerException     if any of the arguments are null
	 * @throws IllegalArgumentException if {@code id}'s format is invalid or a node attempts to modify its own
	 *                                  role
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 * @throws TimeoutException         if the deadline expires before the operation succeeds
	 */
	NodeId setNodeRole(String id, Role role, Instant deadline)
		throws IOException, InterruptedException, TimeoutException;

	/**
	 * Sets the role of a node.
	 *
	 * @param id       the node's ID or hostname
	 * @param role     the new role
	 * @param deadline the absolute time by which the type must change. The method will poll the node's state
	 *                 while the current time is before this value.
	 * @return the node's updated ID
	 * @throws NullPointerException     if any of the arguments are null
	 * @throws IllegalArgumentException if a node attempts to modify its own role
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 * @throws TimeoutException         if the deadline expires before the operation succeeds
	 */
	NodeId setNodeRole(NodeId id, Role role, Instant deadline)
		throws IOException, InterruptedException, TimeoutException;

	/**
	 * Removes a node from the swarm.
	 *
	 * @param id the ID of the node
	 * @return an node remover
	 * @throws NullPointerException     if {@code id} is null
	 * @throws IllegalArgumentException if {@code id}'s format is invalid
	 */
	@CheckReturnValue
	NodeRemover removeNode(String id);

	/**
	 * Removes a node from the swarm.
	 *
	 * @param id the ID of the node
	 * @return an node remover
	 * @throws NullPointerException if {@code id} is null
	 */
	@CheckReturnValue
	NodeRemover removeNode(NodeId id);

	/**
	 * Returns all the swarm services.
	 *
	 * @return an empty list if no match is found
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	List<Service> getServices() throws IOException, InterruptedException;

	/**
	 * Returns the swarm services that match a predicate.
	 *
	 * @param predicate the predicate
	 * @return an empty list if no match is found
	 * @throws NullPointerException     if {@code predicate} is null
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	List<Service> getServices(Predicate<ServiceElement> predicate) throws IOException, InterruptedException;

	/**
	 * Returns a service.
	 *
	 * @param id the ID of the service
	 * @return the service
	 * @throws NullPointerException     if {@code id} is null
	 * @throws IllegalArgumentException if {@code id}'s format is invalid
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	Service getService(String id) throws IOException, InterruptedException;

	/**
	 * Returns a service.
	 *
	 * @param id the ID of the service
	 * @return the service
	 * @throws NullPointerException     if {@code id} is null
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	Service getService(ServiceId id) throws IOException, InterruptedException;

	/**
	 * Lists a service's tasks.
	 *
	 * @param id the service's ID or name
	 * @return the tasks
	 * @throws NullPointerException     if {@code id} is null
	 * @throws IllegalArgumentException if {@code id}'s format is invalid
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	List<Task> getTasksByService(String id) throws IOException, InterruptedException;

	/**
	 * Lists a service's tasks.
	 *
	 * @param id the service's ID or name
	 * @return the tasks
	 * @throws NullPointerException     if {@code id} is null
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	List<Task> getTasksByService(ServiceId id) throws IOException, InterruptedException;

	/**
	 * Creates a service.
	 *
	 * @param imageId the image ID or {@link Image reference} to create the service from
	 * @return a service creator
	 * @throws NullPointerException     if {@code imageId} is null
	 * @throws IllegalArgumentException if {@code imageId}'s format is invalid
	 */
	ServiceCreator createService(String imageId);

	/**
	 * Creates a service.
	 *
	 * @param imageId the image ID or {@link Image reference} to create the service from
	 * @return a service creator
	 * @throws NullPointerException if {@code imageId} is null
	 */
	ServiceCreator createService(ImageId imageId);

	/**
	 * Looks up a task.
	 *
	 * @param id the task's ID
	 * @return null if no match is found
	 * @throws NullPointerException     if {@code id} is null
	 * @throws IllegalArgumentException if {@code id}'s format is invalid
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws FileNotFoundException    if the node's unix socket endpoint does not exist
	 * @throws ConnectException         if the node's TCP/IP socket refused a connection
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	Task getTask(String id) throws IOException, InterruptedException;

	/**
	 * Looks up a task.
	 *
	 * @param id the task's ID
	 * @return null if no match is found
	 * @throws NullPointerException     if {@code id} is null
	 * @throws NotSwarmManagerException if the current node is not a swarm manager
	 * @throws FileNotFoundException    if the node's unix socket endpoint does not exist
	 * @throws ConnectException         if the node's TCP/IP socket refused a connection
	 * @throws IOException              if an I/O error occurs. These errors are typically transient, and
	 *                                  retrying the request may resolve the issue.
	 * @throws InterruptedException     if the thread is interrupted before the operation completes. This can
	 *                                  happen due to shutdown signals.
	 */
	Task getTask(TaskId id) throws IOException, InterruptedException;
}