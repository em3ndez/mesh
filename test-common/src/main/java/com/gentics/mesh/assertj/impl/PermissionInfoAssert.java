package com.gentics.mesh.assertj.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.AbstractAssert;

import com.gentics.mesh.core.rest.common.Permission;
import com.gentics.mesh.core.rest.common.PermissionInfo;

public class PermissionInfoAssert extends AbstractAssert<PermissionInfoAssert, PermissionInfo> {

	public PermissionInfoAssert(PermissionInfo actual) {
		super(actual, PermissionInfoAssert.class);
	}

	public PermissionInfoAssert hasPerm(Permission... permissions) {
		List<String> hasPerm = actual.asMap().entrySet().stream().filter(p -> p.getValue() == true).map(e -> e.getKey().getName())
				.collect(Collectors.toList());
		List<String> mustHave = Arrays.asList(permissions).stream().map(e -> e.getName()).collect(Collectors.toList());
		assertThat(hasPerm).containsAll(mustHave);
		return this;
	}

	public PermissionInfoAssert hasNoPerm(Permission... permissions) {
		List<String> hasPerm = actual.asMap().entrySet().stream().filter(p -> p.getValue() == true).map(e -> e.getKey().getName())
				.collect(Collectors.toList());
		List<String> mustNotHave = Arrays.asList(permissions).stream().map(e -> e.getName()).collect(Collectors.toList());
		assertThat(hasPerm).doesNotContain(mustNotHave.toArray(new String[mustNotHave.size()]));
		return this;

	}

}
