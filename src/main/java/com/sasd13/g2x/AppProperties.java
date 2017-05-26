package com.sasd13.g2x;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppProperties {

	private static Properties properties = new Properties();

	private AppProperties() {
	}

	public static synchronized void init(String[] filePaths) {
		for (String filePath : filePaths) {
			loadProperties(filePath);
		}
	}

	private static void loadProperties(String filePath) {
		InputStream in = null;

		try {
			in = AppProperties.class.getClassLoader().getResourceAsStream(filePath);

			if (in != null) {
				properties.load(in);
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
		}
	}

	public static String getProperty(String key) {
		String value = properties.getProperty(key);

		if (value == null) {
			System.out.println("property key '" + key + "' is unknown");
		}

		return value;
	}

	public static String getProperty(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}
}
