module io.github.cowwoc.canister.buildx.test
{
	requires io.github.cowwoc.canister.core.api;
	requires io.github.cowwoc.canister.core.internal.test;
	requires io.github.cowwoc.canister.buildx.main;
	requires io.github.cowwoc.requirements12.java;
	requires org.slf4j;
	requires ch.qos.logback.core;
	requires ch.qos.logback.classic;
	requires org.apache.commons.compress;
	requires org.bouncycastle.pkix;
	requires org.bouncycastle.provider;
	requires com.fasterxml.jackson.databind;
	requires org.testng;
	requires io.github.cowwoc.canister.core.internal;
	requires io.github.cowwoc.canister.buildx.api;

	exports io.github.cowwoc.canister.buildx.test.resource to org.testng;
}