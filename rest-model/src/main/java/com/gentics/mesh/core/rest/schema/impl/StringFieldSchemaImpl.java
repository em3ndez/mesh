package com.gentics.mesh.core.rest.schema.impl;

import static com.gentics.mesh.core.rest.schema.change.impl.SchemaChangeModel.ALLOW_KEY;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gentics.mesh.core.rest.common.FieldTypes;
import com.gentics.mesh.core.rest.schema.StringFieldSchema;

public class StringFieldSchemaImpl extends AbstractFieldSchema implements StringFieldSchema {

	@JsonProperty("allow")
	private String[] allowedValues;

	@Override
	public String getType() {
		return FieldTypes.STRING.toString();
	}

	@Override
	public String[] getAllowedValues() {
		return allowedValues;
	}

	@Override
	public StringFieldSchema setAllowedValues(String... allowedValues) {
		this.allowedValues = allowedValues;
		return this;
	}

	@Override
	public Map<String, Object> getAllChangeProperties() {
		Map<String, Object> map = super.getAllChangeProperties();
		map.put(ALLOW_KEY, getAllowedValues());
		return map;
	}

	@Override
	public void apply(Map<String, Object> fieldProperties) {
		super.apply(fieldProperties);
		if (fieldProperties.get(ALLOW_KEY) != null) {
			setAllowedValues((String[]) fieldProperties.get(ALLOW_KEY));
		}
	}
}