package com.gentics.mesh.core.data;

import io.vertx.ext.web.RoutingContext;

import java.util.Map;

import com.gentics.mesh.core.rest.node.field.Field;
import com.gentics.mesh.core.rest.schema.FieldSchema;
import com.gentics.mesh.core.rest.schema.Schema;
import com.gentics.mesh.error.MeshSchemaException;

/**
 * A node field container is a aggregation node that holds localized fields.
 *
 */
public interface NodeFieldContainer extends FieldContainer, MicroschemaFieldContainer {

	/**
	 * Locate the field with the given fieldkey in this container and return the rest model for this field.
	 * 
	 * @param rc
	 * @param fieldKey
	 * @param fieldSchema
	 * @param expandField
	 * @return
	 */
	Field getRestField(RoutingContext rc, String fieldKey, FieldSchema fieldSchema, boolean expandField);

	void setFieldFromRest(RoutingContext rc, Map<String, Field> fields, Schema schema) throws MeshSchemaException;

}
