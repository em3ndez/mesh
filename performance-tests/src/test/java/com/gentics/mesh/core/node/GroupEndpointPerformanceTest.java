package com.gentics.mesh.core.node;

import static com.gentics.mesh.test.context.MeshTestHelper.call;
import static com.gentics.mesh.test.performance.StopWatch.loggingStopWatch;

import org.junit.Test;

import com.gentics.mesh.core.rest.group.GroupCreateRequest;
import com.gentics.mesh.parameter.impl.PagingParametersImpl;
import com.gentics.mesh.test.context.AbstractMeshTest;
import com.gentics.mesh.test.context.MeshTestSetting;
import com.gentics.mesh.test.performance.StopWatchLogger;

@MeshTestSetting(useElasticsearch = false, useTinyDataset = false, startServer = true)
public class GroupEndpointPerformanceTest extends AbstractMeshTest {

	private StopWatchLogger logger = StopWatchLogger.logger(getClass());

	private void addGroups() {
		for (int i = 0; i < 200; i++) {
			GroupCreateRequest request = new GroupCreateRequest();
			request.setName("Group" + i);
			call(() -> client().createGroup(request));
		}
	}

	@Test
	public void testPerformance() {
		addGroups();

		String uuid = db().noTx(() -> group().getUuid());

		loggingStopWatch(logger, "group.read-page-100", 200, (step) -> {
			call(() -> client().findGroups(new PagingParametersImpl().setPerPage(100)));
		});

		loggingStopWatch(logger, "group.read-page-25", 200, (step) -> {
			call(() -> client().findGroups(new PagingParametersImpl().setPerPage(25)));
		});

		loggingStopWatch(logger, "group.read-by-uuid", 200, (step) -> {
			call(() -> client().findGroupByUuid(uuid));
		});

		loggingStopWatch(logger, "group.create", 200, (step) -> {
			GroupCreateRequest request = new GroupCreateRequest();
			request.setName("NameNew" + step);
			call(() -> client().createGroup(request));
		});
	}

}
