package com.lucid.lwx.license;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

public class License {
	protected static final String LICENSED_CONTENT = "Licensed by LucidWorks.";
	protected static final String UNLICENSED_CONTENT = "Unlicensed software.";
	protected static final String LICENSE_FILE = ".license.txt";
	public static final long SIXTY_DAYS_EXPIRATION = 5184000000L;
	private String license;
	private long expiration;
	private String licenseFileLocation = ".license.txt";

	public String getLicense() {
		return this.license;
	}

	public long getExpiration() {
		return this.expiration;
	}

	public License(long expiration, String licenseFileLocation) {
		this.expiration = expiration;
		this.licenseFileLocation = licenseFileLocation;
		try {
			init();
		} catch (IOException e) {
			new RuntimeException("Errors with license initialization: " + e);
		}
	}

	public License(long expiration) {
		this.expiration = expiration;
		try {
			init();
		} catch (IOException e) {
			new RuntimeException("Errors with license initialization: " + e);
		}
	}

	public boolean validate() {
		if (this.license.equals("Licensed by LucidWorks."))
			return true;
		if (!isStillValid()) {
			throw new ExpiredLicenseException(
					"The license has expired, please contact Lucid Works for renewal options.");
		}
		return false;
	}

	private void init() throws IOException {
		File file = new File(this.licenseFileLocation);
		if (!file.exists()) {
			createLicenseFile(this.licenseFileLocation, "Unlicensed software.");
		}
		Scanner s = null;
		StringBuffer sb = new StringBuffer();
		try {
			s = new Scanner(new BufferedReader(new FileReader(file)));
			while (s.hasNext()) {
				sb.append(s.next() + " ");
			}
		} finally {
			if (s != null) {
				s.close();
			}
		}
		this.license = sb.toString().trim();
	}

	private boolean isStillValid() {
		long diff = 0L;
		File file = new File(this.licenseFileLocation);
		if (!file.exists()) {
			return false;
		}
		diff = System.currentTimeMillis() - file.lastModified();

		return diff <= this.expiration;
	}

	protected static void createLicenseFile(String licenseFileLocation, String content) throws IOException {
		File file = new File(licenseFileLocation);
		Writer output = new BufferedWriter(new FileWriter(file));
		try {
			output.write(content);
		} finally {
			output.close();
		}
	}
}
