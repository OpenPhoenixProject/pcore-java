/*
  Copyright (c) 2013 Puppet Labs, Inc. and other contributors, as listed below.
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
  <p>
  Contributors:
  Puppet Labs
 */
package com.puppet.pcore.semver.tests;

import com.puppet.pcore.semver.Version;
import com.puppet.pcore.semver.VersionRange;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for VersionRange.
 */
public class VersionRangeTest {

	@Test
	public void dashRange() {
		VersionRange range = VersionRange.create("1.2.0 - 1.3.0");
		assertTrue(range.isIncluded(Version.create(1, 2, 0)));
		assertTrue(range.isIncluded(Version.create(1, 3, 0, "alpha")));
		assertTrue(range.isIncluded(Version.create(1, 3, 0)));
		assertFalse(range.isIncluded(Version.create(1, 2, 0, "alpha")));
		assertFalse(range.isIncluded(Version.create(1, 3, 1)));

		range = VersionRange.create("1.2.0 - 1.3.0-");
		assertTrue(range.isIncluded(Version.create(1, 2, 0)));
		assertFalse(range.isIncluded(Version.create(1, 3, 0, "alpha")));
		assertFalse(range.isIncluded(Version.create(1, 3, 0)));

		range = VersionRange.create("1.2.0- - 1.3.0");
		assertTrue(range.isIncluded(Version.create(1, 2, 0)));
		assertTrue(range.isIncluded(Version.create(1, 3, 0, "alpha")));
		assertTrue(range.isIncluded(Version.create(1, 3, 0)));
		assertTrue(range.isIncluded(Version.create(1, 2, 0, "alpha")));
	}

	@Test
	public void greater() {
		try {
			VersionRange range = VersionRange.create(">1.2.0");
			assertTrue(range.isIncluded(Version.create(1, 3, 0, "alpha")));
			assertTrue(range.isIncluded(Version.create(1, 2, 9, "alpha")));
			assertTrue(range.isIncluded(Version.create(1, 2, 1)));
			assertFalse(range.isIncluded(Version.create(1, 2, 0, "alpha")));
			assertFalse(range.isIncluded(Version.create(1, 2, 0)));

			range = VersionRange.create(">1.2.0--");
			assertTrue(range.isIncluded(Version.create(1, 2, 0, "alpha")));
			assertTrue(range.isIncluded(Version.create(1, 2, 0)));
			assertFalse(range.isIncluded(Version.create(1, 1, 9)));
		} catch(IllegalArgumentException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void greaterEqual() {
		try {
			VersionRange range = VersionRange.create(">=1.2.0");
			assertTrue(range.isIncluded(Version.create(1, 2, 0)));
			assertTrue(range.isIncluded(Version.create(1, 3, 0, "alpha")));
			assertTrue(range.isIncluded(Version.create(1, 2, 9, "alpha")));
			assertTrue(range.isIncluded(Version.create(1, 2, 1)));
			assertFalse(range.isIncluded(Version.create(1, 2, 0, "alpha")));

			range = VersionRange.create(">=1.2.0--");
			assertTrue(range.isIncluded(Version.create(1, 2, 0, "alpha")));
			assertTrue(range.isIncluded(Version.create(1, 2, 0)));
			assertTrue(range.isIncluded(Version.create(1, 2, 0, "-")));
		} catch(IllegalArgumentException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void impossible() {
		testImpossibleRange("1.2.3 < 1.3.0");
		testImpossibleRange("<1.2.3 <=1.3.0");
		testImpossibleRange(">=1.2.3 1.3.0");
		testImpossibleRange(">=1.2.3 >1.3.0");
		testImpossibleRange("1.2.3 1.3.0");
		testImpossibleRange("=1.2.3 - 1.3.0");
		testImpossibleRange("=1.2.x");
		testImpossibleRange("=1.2");
		testImpossibleRange("~1.2.x");
		testImpossibleRange("~1.2.3-alpha");
		testImpossibleRange("~1.2.x-alpha");
		testImpossibleRange("~1.2-alpha");
	}

	@Test
	public void intersection() {
		try {
			VersionRange range1 = VersionRange.create(">1.2.0");
			VersionRange range2 = VersionRange.create("<1.2.0");
			assertNull(range1.intersect(range2));

			range1 = VersionRange.create(">1.2.0");
			range2 = VersionRange.create("<=1.2.0");
			assertNull(range1.intersect(range2));

			range1 = VersionRange.create(">=1.2.0");
			range2 = VersionRange.create("<=1.2.0");
			assertNotNull(range1.intersect(range2));

			range1 = VersionRange.create(">=1.2.0 <1.3.0--");
			range2 = VersionRange.create(">=1.3.0");
			assertNull(range1.intersect(range2));

			range1 = VersionRange.create(">=1.2.0 <1.3.0--");
			range2 = VersionRange.create(">=1.2.1 <1.2.2--");
			assertNotNull(range1.intersect(range2));

			range1 = VersionRange.create("<1.3.0-- >=1.2.0");
			range2 = VersionRange.create("<1.2.2-- >=1.2.1");
			assertNotNull(range1.intersect(range2));
		} catch(IllegalArgumentException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void less() {
		try {
			VersionRange range = VersionRange.create("<1.2.0");
			assertTrue(range.isIncluded(Version.create(1, 2, 0, "alpha")));
			assertTrue(range.isIncluded(Version.create(1, 1, 9, "alpha")));
			assertTrue(range.isIncluded(Version.create(1, 1, 9)));
			assertFalse(range.isIncluded(Version.create(1, 3, 0, "alpha")));
			assertFalse(range.isIncluded(Version.create(1, 2, 0)));

			range = VersionRange.create("<1.2.0--");
			assertFalse(range.isIncluded(Version.create(1, 2, 0, "alpha")));
			assertTrue(range.isIncluded(Version.create(1, 1, 9, "alpha")));
		} catch(IllegalArgumentException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void lessEqual() {
		try {
			VersionRange range = VersionRange.create("<=1.2.0");
			assertTrue(range.isIncluded(Version.create(1, 2, 0, "alpha")));
			assertTrue(range.isIncluded(Version.create(1, 1, 9, "alpha")));
			assertTrue(range.isIncluded(Version.create(1, 1, 9)));
			assertFalse(range.isIncluded(Version.create(1, 3, 0, "alpha")));
			assertTrue(range.isIncluded(Version.create(1, 2, 0)));

			range = VersionRange.create("<=1.2.0--");
			assertFalse(range.isIncluded(Version.create(1, 2, 0, "alpha")));
			assertTrue(range.isIncluded(Version.create(1, 2, 0, "-")));
			assertTrue(range.isIncluded(Version.create(1, 1, 9, "alpha")));
		} catch(IllegalArgumentException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void majorMinorX() {
		try {
			VersionRange range = VersionRange.create("1.2.x");
			assertFalse(range.isIncluded(Version.create(1, 1, 9)));
			assertFalse(range.isIncluded(Version.create(1, 3, 0, "alpha")));
			assertFalse(range.isIncluded(Version.create(1, 2, 0, "alpha")));
			assertTrue(range.isIncluded(Version.create(1, 2, 0)));
			assertTrue(range.isIncluded(Version.create(1, 2, 2)));
		} catch(IllegalArgumentException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void majorX() {
		try {
			VersionRange range = VersionRange.create("1.x");
			assertFalse(range.isIncluded(Version.create(0, 9, 9)));
			assertFalse(range.isIncluded(Version.create(2, 0, 0, "alpha")));
			assertFalse(range.isIncluded(Version.create(1, 0, 0, "alpha")));
			assertTrue(range.isIncluded(Version.create(1, 0, 0)));
			assertTrue(range.isIncluded(Version.create(1, 0, 2)));
			assertTrue(range.isIncluded(Version.create(1, 2, 0)));
		} catch(IllegalArgumentException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void specific() {
		try {
			VersionRange range = VersionRange.exact(Version.create(1, 0, 0));
			assertFalse(range.isIncluded(Version.create(0, 9, 9)));
			assertFalse(range.isIncluded(Version.create(1, 0, 1)));
			assertFalse(range.isIncluded(Version.create(1, 0, 0, "alpha")));
			assertTrue(range.isIncluded(Version.fromString("1.0.0")));

			range = VersionRange.create("1.0.0");
			assertFalse(range.isIncluded(Version.create(0, 9, 9)));
			assertFalse(range.isIncluded(Version.create(1, 0, 1)));
			assertFalse(range.isIncluded(Version.create(1, 0, 0, "alpha")));
			assertTrue(range.isIncluded(Version.fromString("1.0.0")));

			range = VersionRange.create(">=1.0.0 <=1.0.0");
			assertFalse(range.isIncluded(Version.create(0, 9, 9)));
			assertFalse(range.isIncluded(Version.create(1, 0, 1)));
			assertFalse(range.isIncluded(Version.create(1, 0, 0, "alpha")));
			assertTrue(range.isIncluded(Version.fromString("1.0.0")));
		} catch(IllegalArgumentException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void tildeVersions() {
		try {
			VersionRange range = VersionRange.create("~1.2.3");
			assertFalse(range.isIncluded(Version.create(1, 2, 3, "alpha")));
			assertTrue(range.isIncluded(Version.create(1, 2, 3)));
			assertTrue(range.isIncluded(Version.create(1, 2, 10)));

			assertFalse(range.isIncluded(Version.create(1, 3, 0, "alpha")));
			assertFalse(range.isIncluded(Version.create(1, 3, 0)));

			range = VersionRange.create("~1.2");
			assertFalse(range.isIncluded(Version.create(1, 2, 0, "alpha")));
			assertTrue(range.isIncluded(Version.create(1, 2, 0)));
			assertTrue(range.isIncluded(Version.create(1, 2, 10)));

			assertFalse(range.isIncluded(Version.create(1, 3, 0, "alpha")));
			assertFalse(range.isIncluded(Version.create(1, 3, 0)));

			range = VersionRange.create("~1");
			assertFalse(range.isIncluded(Version.create(1, 0, 0, "alpha")));
			assertTrue(range.isIncluded(Version.create(1, 0, 0)));
			assertTrue(range.isIncluded(Version.create(1, 0, 10)));

			assertFalse(range.isIncluded(Version.create(1, 1, 0, "alpha")));
			assertFalse(range.isIncluded(Version.create(1, 1, 0)));
		} catch(IllegalArgumentException e) {
			fail(e.getMessage());
		}
	}

	private void testImpossibleRange(String range) {
		try {
			VersionRange.create(range);
			fail("should not create impossible range '" + range + '\'');
		} catch(IllegalArgumentException ignored) {
		}
	}
}
