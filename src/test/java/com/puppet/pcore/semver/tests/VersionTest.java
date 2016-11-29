package com.puppet.pcore.semver.tests;

import com.puppet.pcore.semver.Version;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Version.
 */
public class VersionTest {
	@Test
	public void badNegativeNumbers() {
		try {
			Version.create(1, 0, -1);
			fail("should not create version with negative numbers");
		} catch(IllegalArgumentException ignored) {
		}
	}

	@Test
	public void badPreReleaseSeparator() {
		try {
			Version.fromString("0.0.0.alpha");
			fail("should not permit '.' as pre-release separator");
		} catch(IllegalArgumentException ignored) {
		}
	}

	@Test
	public void badPreReleaseString() {
		try {
			Version.fromString("0.0.0-bad=qualifier");
			fail("should not create version illegal characters in pre-release");
		} catch(IllegalArgumentException ignored) {
		}
	}

	@Test
	public void emptyPreReleaseLessThanNoPreRelease() {
		try {
			assertTrue(Version.fromString("1.0.0").compareTo(Version.fromString("1.0.0-")) > 0);
		} catch(IllegalArgumentException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void emptyPreReleaseLessThanOtherPreRelease() {
		try {
			assertTrue(Version.fromString("1.0.0-alpha").compareTo(Version.fromString("1.0.0-")) > 0);
		} catch(IllegalArgumentException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void numbersMagnitude() {
		try {
			assertTrue(Version.fromString("0.0.1").compareTo(Version.fromString("0.0.2")) < 0);
			assertTrue(Version.fromString("0.0.9").compareTo(Version.fromString("0.1.0")) < 0);
			assertTrue(Version.fromString("0.9.9").compareTo(Version.fromString("1.0.0")) < 0);
			assertTrue(Version.fromString("1.9.0").compareTo(Version.fromString("1.10.0")) < 0);
			assertTrue(Version.fromString("1.10.0").compareTo(Version.fromString("1.11.0")) < 0);
		} catch(IllegalArgumentException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void okStringVersions() {
		try {
			assertEquals(Version.create(0, 0, 0), Version.fromString("0.0.0"));
			assertEquals(Version.create(0, 0, 1), Version.fromString("0.0.1"));
			assertEquals(Version.create(0, 1, 0), Version.fromString("0.1.0"));
			assertEquals(Version.create(1, 0, 0), Version.fromString("1.0.0"));
			assertEquals(Version.create(0, 0, 0, "alpha"), Version.fromString("0.0.0-alpha"));
			assertEquals(Version.create(0, 0, 1, "alpha"), Version.fromString("0.0.1-alpha"));
			assertEquals(Version.create(0, 1, 0, "alpha"), Version.fromString("0.1.0-alpha"));
			assertEquals(Version.create(1, 0, 0, "alpha"), Version.fromString("1.0.0-alpha"));
		} catch(IllegalArgumentException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void preReleaseLessThanVersion() {
		try {
			assertTrue(Version.fromString("1.0.0").compareTo(Version.fromString("1.0.0-alpha")) > 0);
		} catch(IllegalArgumentException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void preReleaseMagnitude() {
		try {
			assertTrue(Version.fromString("1.0.0-alpha1").compareTo(Version.fromString("1.0.0-beta1")) < 0);
			assertTrue(Version.fromString("1.0.0-beta1").compareTo(Version.fromString("1.0.0-beta2")) < 0);
			assertTrue(Version.fromString("1.0.0-beta2").compareTo(Version.fromString("1.0.0-rc1")) < 0);
		} catch(IllegalArgumentException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void tooFewDigits() {
		try {
			Version.fromString("0.0");
			fail("should not create version with just two digits");
		} catch(IllegalArgumentException ignored) {
		}
	}
}
