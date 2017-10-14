package com.gentics.mesh.core.rest.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gentics.mesh.core.rest.common.RestModel;
import com.gentics.mesh.core.rest.schema.SchemaReference;
import com.gentics.mesh.core.rest.schema.impl.SchemaReferenceImpl;

public class ProjectCreateRequest implements RestModel {

	@JsonProperty(required = true)
	@JsonPropertyDescription("Name of the project")
	private String name;

	@JsonProperty(required = true)
	@JsonPropertyDescription("Reference to the schema of the root node. Creating a project will also automatically create the base node of the project and link the schema to the initial release of the project.")
	@JsonDeserialize(as = SchemaReferenceImpl.class)
	private SchemaReference schema;

	/**
	 * Return the project name.
	 * 
	 * @return Name of the project
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the project name.
	 * 
	 * @param name
	 *            Name of the project
	 * @return Fluent API
	 */
	public ProjectCreateRequest setName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Return the schema reference for the project create request.
	 * 
	 * @return Schema reference for the root node of the project
	 */
	public SchemaReference getSchema() {
		return schema;
	}

	/**
	 * Set the schema reference for the project. The reference is used to define the type of root node of the project.
	 * 
	 * @param schemaReference
	 *            Schema reference for the root node of the project
	 * @return Fluent API
	 */
	public ProjectCreateRequest setSchema(SchemaReference schemaReference) {
		this.schema = schemaReference;
		return this;
	}

	/**
	 * Shortcut method to directly set the schema reference with the given name.
	 * 
	 * @param schemaName
	 *            Name of the schema
	 * @return
	 */
	public ProjectCreateRequest setSchemaRef(String schemaName) {
		setSchema(new SchemaReferenceImpl().setName(schemaName));
		return this;
	}

}