[![Maven Central](https://maven-badges.sml.io/maven-central/io.github.cowwoc.canister/canister/badge.svg)](https://search.maven.org/search?q=g:io.github.cowwoc.canister)
[![build-status](https://github.com/cowwoc/canister/workflows/Build/badge.svg)](https://github.com/cowwoc/canister/actions/?query=workflow%3Abuild)

# <img src="docs/logo.svg" width=64 height=64 alt="logo"> Canister

[![API](https://img.shields.io/badge/api_docs-5B45D5.svg)](https://cowwoc.github.io/canister/0.10/)
[![Changelog](https://img.shields.io/badge/changelog-A345D5.svg)](docs/changelog.md)

A modern Java client for [Docker](https://www.docker.com/): fluent API, test-friendly, and Kubernetes-ready.

✅ A fluent, strongly typed API.<br>
✅ Support for building images with and without root access.<br>
✅ Easy integration with CI and tests.<br>
✅ First-class support for capturing stdout/stderr and process exit status.<br>

To get started, add this Maven dependency:

```xml

<dependency>
  <groupId>io.github.cowwoc.canister</groupId>
  <artifactId>canister</artifactId>
  <version>0.10</version>
</dependency>
```

## Example

```java
import io.github.cowwoc.canister.core.id.ImageId;
import io.github.cowwoc.canister.core.resource.ImageBuilder.Exporter;
import io.github.cowwoc.canister.docker.api.client.Docker;
import io.github.cowwoc.canister.docker.api.client.DockerClient;

import java.io.IOException;

class Example
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
}
```

## Getting Started

See the [API documentation](https://cowwoc.github.io/canister/0.10/) for more details.

## 💖 Support Ongoing Development 💖

If you find this project helpful, please consider [sponsoring me](https://github.com/sponsors/cowwoc).
Maintaining quality open-source software takes significant time and effort—especially while balancing family
life with young children. Your support helps make this work sustainable.

## Missing Features?

The `canister` API covers a wide range of functionality, and since my time is limited, new features are added
based on user requests. If there's a property or capability you'd like to see added,
please [open a new issue](issues/new).

## Licensing

This library is dual-licensed:

- ✅ [ModernJDK License](docs/modern-jdk-license-1.0.md) (free for personal use or small businesses):
  - You may use, modify, and redistribute this software for free when:
    - It is compiled for and executed on the latest generally available (GA) Java Development Kit (JDK)
      version at the time of deployment.
    - You are an individual or a company with 10 or fewer employees.
    - You do not rebrand or white-label the software.
  - You are not required to update existing deployments when a newer JDK is released.

- 💼 [Commercial License](docs/commercial-license-1.0.md):
  - Required for:
    - Compiling, running, or distributing the software for older (non-GA) JDK versions.
    - Use by companies with more than 10 employees.
    - White-labeling or rebranding the library in any form.

## Dependencies

* See [Third party licenses](LICENSE-3RD-PARTY.md) for the licenses of the dependencies.