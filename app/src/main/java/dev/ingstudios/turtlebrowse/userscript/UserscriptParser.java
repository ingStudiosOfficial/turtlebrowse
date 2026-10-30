package dev.ingstudios.turtlebrowse.userscript;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class UserscriptParser {
	public static UserscriptMetadata parse(String input, String filename) {
		final Pattern pattern = Pattern.compile(
				"\\B(// ==UserScript==\\r?\\n(.*?)\\r?\\n// ==/UserScript==)(.*)",
				Pattern.DOTALL);
		final Matcher matcher = pattern.matcher(input);

		if (matcher.find()) {
			final String metablock = matcher.group(1);
			final String metas = matcher.group(2);

			final JsonObject meta = new JsonObject();

			final List<String> metasArray = new ArrayList<>(Arrays.asList(metas.split("\\R")));
			System.out.printf("Metas array length: %d\n", metasArray.size());

			final Pattern partsPattern = Pattern.compile("^\\s*//\\s*@([\\w-]+)\\s+(.+)$");

			metasArray.forEach(item -> {
				final Matcher partsMatcher = partsPattern.matcher(item);
				if (partsMatcher.matches()) {
					System.out.println("Found part.");
					final String key = partsMatcher.group(1);
					final String value = partsMatcher.group(2);
					final JsonArray array = meta.has(key) ? meta.getAsJsonArray(key) : new JsonArray();
					array.add(value);
					meta.add(key, array);
				}
			});

			return new UserscriptMetadata(meta, metablock, input, filename);
		}

		return null;
	}

	public record UserscriptMetadata(JsonObject meta, String metablock, String content, String filename) {
	}
}
