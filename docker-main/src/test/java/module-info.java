module io.github.cowwoc.canister.docker.test
{
	requires transitive io.github.cowwoc.canister.docker.api;
	requires io.github.cowwoc.canister.core.internal;
	requires io.github.cowwoc.canister.core.internal.test;
	requires io.github.cowwoc.canister.buildx.api;
	requires io.github.cowwoc.canister.docker.main;
	requires io.github.cowwoc.pouch.core;
	requires io.github.cowwoc.requirements12.java;
	requires org.slf4j;
	requires ch.qos.logback.core;
	requires ch.qos.logback.classic;
	requires org.apache.commons.compress;
	requires org.bouncycastle.pkix;
	requires org.bouncycastle.provider;
	requires com.fasterxml.jackson.annotation;
	requires org.testng;
	requires java.desktop;

	exports io.github.cowwoc.canister.docker.test to org.testng;
	exports io.github.cowwoc.canister.docker.test.resource to org.testng;
}