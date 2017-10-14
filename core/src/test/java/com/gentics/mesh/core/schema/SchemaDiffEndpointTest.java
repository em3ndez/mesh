package com.gentics.mesh.core.schema;

import static com.gentics.mesh.assertj.MeshAssertions.assertThat;
import static com.gentics.mesh.core.rest.schema.change.impl.SchemaChangeModel.DISPLAY_FIELD_NAME_KEY;
import static com.gentics.mesh.core.rest.schema.change.impl.SchemaChangeOperation.ADDFIELD;
import static com.gentics.mesh.core.rest.schema.change.impl.SchemaChangeOperation.REMOVEFIELD;
import static com.gentics.mesh.core.rest.schema.change.impl.SchemaChangeOperation.UPDATESCHEMA;
import static com.gentics.mesh.test.ClientHelper.call;
import static com.gentics.mesh.test.TestSize.FULL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.gentics.mesh.FieldUtil;
import com.gentics.mesh.core.data.schema.SchemaContainer;
import com.gentics.mesh.core.rest.error.GenericRestException;
import com.gentics.mesh.core.rest.schema.BinaryFieldSchema;
import com.gentics.mesh.core.rest.schema.HtmlFieldSchema;
import com.gentics.mesh.core.rest.schema.NodeFieldSchema;
import com.gentics.mesh.core.rest.schema.Schema;
import com.gentics.mesh.core.rest.schema.StringFieldSchema;
import com.gentics.mesh.core.rest.schema.change.impl.SchemaChangesListModel;
import com.gentics.mesh.core.rest.schema.impl.HtmlFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.SchemaModelImpl;
import com.gentics.mesh.core.rest.schema.impl.StringFieldSchemaImpl;
import com.gentics.mesh.test.context.AbstractMeshTest;
import com.gentics.mesh.test.context.MeshTestSetting;
import com.syncleus.ferma.tx.Tx;

@MeshTestSetting(useElasticsearch = false, testSize = FULL, startServer = true)
public class SchemaDiffEndpointTest extends AbstractMeshTest {

	private Schema getSchema() {
		Schema schema = new SchemaModelImpl();
		schema.setName("content");
		schema.setDescription("Content schema for blogposts");
		schema.setDisplayField("title");
		schema.setSegmentField("slug");

		StringFieldSchema slugFieldSchema = new StringFieldSchemaImpl();
		slugFieldSchema.setName("slug");
		slugFieldSchema.setLabel("Slug");
		slugFieldSchema.setRequired(true);
		schema.addField(slugFieldSchema);

		StringFieldSchema titleFieldSchema = new StringFieldSchemaImpl();
		titleFieldSchema.setName("title");
		titleFieldSchema.setLabel("Title");
		schema.addField(titleFieldSchema);

		StringFieldSchema nameFieldSchema = new StringFieldSchemaImpl();
		nameFieldSchema.setName("teaser");
		nameFieldSchema.setLabel("Teaser");
		nameFieldSchema.setRequired(true);
		schema.addField(nameFieldSchema);

		HtmlFieldSchema contentFieldSchema = new HtmlFieldSchemaImpl();
		contentFieldSchema.setName("content");
		contentFieldSchema.setLabel("Content");
		schema.addField(contentFieldSchema);

		schema.setContainer(false);
		return schema;
	}

	@Test
	public void testDiffDisplayField() throws GenericRestException, Exception {
		try (Tx tx = tx()) {
			SchemaContainer container = schemaContainer("content");
			Schema request = getSchema();
			request.setDisplayField("slug");

			SchemaChangesListModel changes = call(() -> client().diffSchema(container.getUuid(), request));
			assertNotNull(changes);
			// We expect one change that indicates that the displayField property has changed.
			assertThat(changes.getChanges()).hasSize(1);
			assertThat(changes.getChanges().get(0)).is(UPDATESCHEMA).hasProperty(DISPLAY_FIELD_NAME_KEY, "slug");
		}
	}

	@Test
	public void testNoDiff() {
		try (Tx tx = tx()) {
			SchemaContainer container = schemaContainer("content");
			Schema request = getSchema();
			SchemaChangesListModel changes = call(() -> client().diffSchema(container.getUuid(), request));
			assertNotNull(changes);
			assertThat(changes.getChanges()).isEmpty();
		}
	}

	@Test
	public void testAddField() {
		try (Tx tx = tx()) {
			SchemaContainer schema = schemaContainer("content");
			Schema request = getSchema();
			BinaryFieldSchema binaryField = FieldUtil.createBinaryFieldSchema("binary");
			binaryField.setAllowedMimeTypes("one", "two");
			request.addField(binaryField);

			SchemaChangesListModel changes = call(() -> client().diffSchema(schema.getUuid(), request));
			assertNotNull(changes);
			assertThat(changes.getChanges()).hasSize(2);
			assertThat(changes.getChanges().get(0)).is(ADDFIELD).forField("binary");
			assertThat(changes.getChanges().get(1)).is(UPDATESCHEMA).hasProperty("order",
					new String[] { "slug", "title", "teaser", "content", "binary" });
		}
	}

	@Test
	public void testAddNodeField() {
		try (Tx tx = tx()) {
			SchemaContainer schema = schemaContainer("content");
			Schema request = getSchema();
			NodeFieldSchema nodeField = FieldUtil.createNodeFieldSchema("node");
			nodeField.setAllowedSchemas("content");
			request.addField(nodeField);

			SchemaChangesListModel changes = call(() -> client().diffSchema(schema.getUuid(), request));
			assertNotNull(changes);
			assertThat(changes.getChanges()).hasSize(2);
			assertThat(changes.getChanges().get(0)).is(ADDFIELD).forField("node").hasProperty("allow", new String[] {"content"});
			assertThat(changes.getChanges().get(1)).is(UPDATESCHEMA).hasProperty("order",
					new String[] { "slug", "title", "teaser", "content", "node" });
		}
	}

	@Test
	public void testDefaultMigration() {
		try (Tx tx = tx()) {
			SchemaContainer schema = schemaContainer("content");
			Schema request = getSchema();
			request.removeField("content");

			SchemaChangesListModel changes = call(() -> client().diffSchema(schema.getUuid(), request));
			assertNotNull(changes);
			assertThat(changes.getChanges()).hasSize(2);
			assertThat(changes.getChanges().get(0)).is(REMOVEFIELD).forField("content");
			assertThat(changes.getChanges().get(1)).is(UPDATESCHEMA).hasProperty("order", new String[] { "slug", "title", "teaser" });
			assertNotNull("A default migration script should have been added to the change.", changes.getChanges().get(0).getMigrationScript());
		}
	}

}
