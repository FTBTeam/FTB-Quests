package com.feed_the_beast.ftbquests.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public class FileUtils {
	public static List<String> read(InputStream in) throws IOException {
		List<String> list = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;

		while ((line = reader.readLine()) != null) {
			list.add(line);
		}

		return list;
	}

	public static List<String> readFile(File file) {
		try (InputStream in = new FileInputStream(file)) {
			return read(in);
		} catch (IOException ex) {
			return Collections.emptyList();
		}
	}

	public static void delete(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();

			if (files != null) {
				for (File f : files) {
					delete(f);
				}
			}
		} else {
			file.delete();
		}
	}
}