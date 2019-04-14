package com.lucid.lwx.license;

import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testng.AssertJUnit;

public class LicenseTest {
	@Before
	public void setup() throws Exception {
		removeLicenseFile(".license.txt");
	}

	@After
	public void tearDown() throws Exception {
		removeLicenseFile(".license.txt");
	}

	@Test
	public void testUnlicensed() throws Exception {
		License license = new License(5184000000L);
		AssertJUnit.assertFalse(license.validate());
		AssertJUnit.assertEquals("Unlicensed software.", license.getLicense());
	}

	@Test
	public void testDirDoesntExistWontBlowup() throws Exception {
		License license = new License(5184000000L, ".license.txt-non-existent");
		AssertJUnit.assertFalse(license.validate());
		AssertJUnit.assertEquals("Unlicensed software.", license.getLicense());
		removeLicenseFile(".license.txt-non-existent");
	}

	@Test
	public void testInitTwice() throws Exception {
		License license = new License(5184000000L);
		AssertJUnit.assertFalse(license.validate());
		AssertJUnit.assertEquals("Unlicensed software.", license.getLicense());
		license = new License(5184000000L);
		AssertJUnit.assertFalse(license.validate());
		AssertJUnit.assertEquals("Unlicensed software.", license.getLicense());
	}

	@Test
	public void testLicensed() throws IOException {
		License.createLicenseFile(".license.txt", "Licensed by LucidWorks.");
		License license = new License(5184000000L);
		AssertJUnit.assertTrue(license.validate());
		AssertJUnit.assertEquals("Licensed by LucidWorks.", license.getLicense());
	}

	@Test(expected = ExpiredLicenseException.class)
	public void testExpired() throws Exception {
		License license = new License(10L);
		Thread.sleep(100L);
		AssertJUnit.assertEquals(10L, license.getExpiration());
		license.validate();
	}

	@Test(expected = ExpiredLicenseException.class)
	public void testExpiredReinitializedBlowsUp() throws Exception {
		License license = new License(100L);
		Thread.sleep(200L);
		license = new License(100L);
		license.validate();
	}

	@Test
	public void testExpiredGoodLicenseFileWontThrow() throws Exception {
		License.createLicenseFile(".license.txt", "Licensed by LucidWorks.");
		License license = new License(10L);
		Thread.sleep(100L);
		license.validate();
	}

	private boolean removeLicenseFile(String fileLocation) throws IOException {
		File file = new File(fileLocation);
		boolean deleted = false;
		if (file.exists())
			deleted = file.delete();
		return deleted;
	}
}
