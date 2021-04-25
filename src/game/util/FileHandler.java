package game.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileHandler {

	/**
	 * Reads a text file from the jar in the package src/res/files/
	 *
	 * @param fileName the filename in the package
	 * @return the text of the file
	 */
	public static String loadFile(String fileName) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("res/files/" + fileName), StandardCharsets.UTF_8));

			StringBuilder source = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				source.append(line).append("\n");
			}
			reader.close();

			return source.toString();
		} catch (Exception e) {
			ErrorUtil.printError(String.format("Loading file: %s", fileName));
		}
		return null;
	}

	public static String loadFile(File file) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));

			StringBuilder source = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				source.append(line).append("\n");
			}
			reader.close();

			return source.toString();
		} catch (Exception e) {
			ErrorUtil.printError(String.format("Loading file: %s", file.getName()));
		}
		return null;
	}
}
