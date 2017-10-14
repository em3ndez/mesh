package com.gentics.mesh.search.transformer;

import static com.gentics.mesh.test.TestSize.FULL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import com.syncleus.ferma.tx.Tx;
import com.gentics.mesh.core.data.ContainerType;
import com.gentics.mesh.core.data.NodeGraphFieldContainer;
import com.gentics.mesh.core.data.Release;
import com.gentics.mesh.search.index.node.NodeContainerTransformer;
import com.gentics.mesh.test.context.AbstractMeshTest;
import com.gentics.mesh.test.context.MeshTestSetting;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@MeshTestSetting(useElasticsearch = false, testSize = FULL, startServer = false)
public class NodeContainerTransformerTest extends AbstractMeshTest {

	@Test
	public void testNodeTagFamilyTransformer() {
		NodeContainerTransformer transformer = new NodeContainerTransformer();
		try (Tx tx = tx()) {
			Release release = project().getLatestRelease();
			NodeGraphFieldContainer node = content("concorde").getGraphFieldContainer(english(), release, ContainerType.PUBLISHED);
			JsonObject document = transformer.toDocument(node, release.getUuid());
			JsonObject families = document.getJsonObject("tagFamilies");

			HashSet<String> basicNames = new HashSet<>(Arrays.asList("Plane", "Twinjet"));
			HashSet<String> colorNames = new HashSet<>(Arrays.asList("red"));

			JsonArray basicArray = families.getJsonObject("basic").getJsonArray("tags");
			JsonArray colorArray = families.getJsonObject("colors").getJsonArray("tags");

			assertEquals("Incorrect count of basic tags", basicNames.size(), basicArray.size());
			assertEquals("Incorrect count of colors", colorNames.size(), colorArray.size());

			boolean allTagsContained = basicArray.stream()
				.map(obj -> ((JsonObject)obj).getString("name"))
				.allMatch(name -> basicNames.contains(name));
			boolean allColorsContained = colorArray.stream()
				.map(obj -> ((JsonObject)obj).getString("name"))
				.allMatch(name -> colorNames.contains(name));

			assertTrue("Could not find all basic tags", allTagsContained);
			assertTrue("Could not find all colors", allColorsContained);
		}
	}
}
