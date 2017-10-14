package com.gentics.mesh.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.gentics.mesh.util.UUIDUtil;

/**
 * Some tests for the uuid utils.
 */
public class UUIDUtilTest {

	@Test
	public void testIsUUID() {
		String validUUID = "dd5e85cebb7311e49640316caf57479f";
		String uuid = UUIDUtil.randomUUID();
		System.out.println(uuid);
		assertTrue("The uuid {" + uuid + "} is not a valid uuid.", UUIDUtil.isUUID(uuid));
		assertFalse(UUIDUtil.isUUID(""));
		assertFalse(UUIDUtil.isUUID("123"));
		assertFalse(UUIDUtil.isUUID("-1"));
		assertFalse(UUIDUtil.isUUID("1"));
		assertFalse(UUIDUtil.isUUID("0"));
		assertFalse(UUIDUtil.isUUID("/test/1235.html"));
		assertTrue(UUIDUtil.isUUID(validUUID));
		assertFalse(UUIDUtil.isUUID(validUUID.replace("f", "z")));
	}
}
