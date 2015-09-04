package com.gentics.mesh.core.integration;

import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.gentics.mesh.cli.Mesh;
import com.gentics.mesh.test.AbstractIntegrationTest;

public class RestIntegrationTest extends AbstractIntegrationTest {

	@Test
	public void testIntegration() throws Exception {
		long timeout = DEFAULT_TIMEOUT_SECONDS * 2;
		final CountDownLatch latch = new CountDownLatch(1);

		final Mesh mesh = Mesh.initalize();
		mesh.setCustomLoader((vertx) -> {
			vertx.eventBus().consumer("mesh-startup-complete", mh -> {
				latch.countDown();
			});
		});

		new Thread(() -> {
			try {
				mesh.run();
			} catch (Exception e) {
				fail("Error while starting instance: " + e.getMessage());
				e.printStackTrace();
			}
		}).start();
		if (!latch.await(timeout, TimeUnit.SECONDS)) {
			fail("Mesh did not startup on time. Timeout {" + timeout + "} seconds reached.");
		}
	}
}
