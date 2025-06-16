package io.github.cowwoc.canister.docker.test.resource;

import io.github.cowwoc.canister.docker.api.client.DockerClient;
import io.github.cowwoc.canister.docker.test.IntegrationTestContainer;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public final class NetworkIT
{
	@Test
	public void get() throws IOException, InterruptedException, TimeoutException
	{
		IntegrationTestContainer it = new IntegrationTestContainer();
		DockerClient docker = it.getClient();
		docker.getNetwork("default");
		it.onSuccess();
	}
}