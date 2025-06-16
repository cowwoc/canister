package io.github.cowwoc.canister.docker.test;

import io.github.cowwoc.canister.core.id.ImageId;
import io.github.cowwoc.canister.core.resource.ImageBuilder.Exporter;
import io.github.cowwoc.canister.docker.api.client.Docker;
import io.github.cowwoc.canister.docker.api.client.DockerClient;

import java.io.IOException;

public final class Readme
{
	public static void main(String[] args) throws IOException, InterruptedException
	{
		try (DockerClient docker = Docker.fromPath())
		{
			ImageId id = docker.buildImage().
				export(Exporter.dockerImage().build()).
				apply(".");

			docker.tagImage(id, "rocket-ship");
			docker.pushImage("rocket-ship").apply();
		}
	}

	private Readme()
	{
	}
}