package io.github.cowwoc.canister.docker.test.resource;

import io.github.cowwoc.canister.docker.api.client.DockerClient;
import io.github.cowwoc.canister.docker.id.ContextId;
import io.github.cowwoc.canister.docker.resource.Context;
import io.github.cowwoc.canister.docker.test.IntegrationTestContainer;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

public final class ContextIT
{
	@Test
	public void getClientContext() throws IOException, InterruptedException, TimeoutException
	{
		IntegrationTestContainer it = new IntegrationTestContainer();
		DockerClient client = it.getClient();
		ContextId clientContext = client.getClientContext();
		requireThat(clientContext.getValue(), "clientContext").isEqualTo(it.getName());
		it.onSuccess();
	}

	@Test
	public void listContexts() throws IOException, InterruptedException, TimeoutException
	{
		IntegrationTestContainer it = new IntegrationTestContainer();
		DockerClient client = it.getClient();
		List<Context> contexts = client.getContexts();
		boolean matchFound = false;
		for (Context context : contexts)
		{
			if (context.getId().getValue().equals(it.getName()))
			{
				matchFound = true;
				break;
			}
		}
		requireThat(matchFound, "matchFound").withContext(contexts, "contexts").isTrue();
		it.onSuccess();
	}
}