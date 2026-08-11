package dev.ingstudios.turtlebrowse.db;

import static org.dizitart.no2.filters.FluentFilter.where;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.mvstore.MVStoreModule;

import dev.ingstudios.turtlebrowse.Main;

public class ProfileDatabase {
	private static ProfileDatabase instance;
	private Nitrite db;

	private NitriteCollection settingsCollection;

	private ProfileDatabase(String profileId) {
		initDb(profileId);
	}

	private void initDb(String profileId) {
		final Path dbPath = Main.getStoragePath("profiles", profileId, "main.db");
		final Path parentDir = dbPath.getParent();
		try {
			if (parentDir != null && Files.notExists(parentDir)) {
				Files.createDirectories(parentDir);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		final MVStoreModule storeModule = MVStoreModule.withConfig().filePath(dbPath.toString()).build();

		db = Nitrite.builder().loadModule(storeModule).openOrCreate();

		settingsCollection = db.getCollection("settings");
	}

	public static synchronized ProfileDatabase getInstance(String profileId) {
		if (instance == null) {
			instance = new ProfileDatabase(profileId);
		}
		return instance;
	}

	public String getDefaultSearchEngine() {
		final Document searchEngineDocument = settingsCollection.find(where("setting").eq("searchEngine"))
				.firstOrNull();

		if (searchEngineDocument == null) {
			final Document newSearchEngineDocument = Document.createDocument().put("setting", "searchEngine")
					.put("engine", "brave");
			settingsCollection.insert(newSearchEngineDocument);
			return "brave";
		}

		return searchEngineDocument.get("engine").toString();
	}

	public void setDefaultSearchEngine(String searchEngine) {
		System.out.printf("Setting search engine: %s\n", searchEngine);

		final Document searchEngineDocument = settingsCollection.find(where("setting").eq("searchEngine"))
				.firstOrNull();

		if (searchEngineDocument == null) {
			System.err.println("Search engine document is null.");
			final Document newSearchEngineDocument = Document.createDocument().put("setting", "searchEngine")
					.put("engine", searchEngine);
			settingsCollection.insert(newSearchEngineDocument);
			return;
		}

		searchEngineDocument.put("engine", searchEngine);

		settingsCollection.update(searchEngineDocument);
	}

	public boolean getDiscordPresenceSetting() {
		final Document discordDocument = settingsCollection.find(where("setting").eq("discordPresence"))
				.firstOrNull();

		if (discordDocument == null) {
			final Document newDiscordDocument = Document.createDocument().put("setting", "discordPresence")
					.put("enabled", false);
			settingsCollection.insert(newDiscordDocument);
			return true;
		}

		return Boolean.valueOf(discordDocument.get("enabled").toString());
	}

	public void setDiscordPresenceSetting(boolean enabled) {
		System.out.printf("Setting Discord presence setting: %s\n", enabled);

		final Document discordDocument = settingsCollection.find(where("setting").eq("discordPresence"))
				.firstOrNull();

		if (discordDocument == null) {
			System.err.println("Discord document is null.");
			final Document newDiscordDocument = Document.createDocument().put("setting", "discordPresence")
					.put("enabled", enabled);
			settingsCollection.insert(newDiscordDocument);
			return;
		}

		discordDocument.put("enabled", enabled);

		settingsCollection.update(discordDocument);
	}

	public AISettings getAISettings() {
		final Document aiDocument = settingsCollection.find(where("setting").eq("aiFeatures"))
				.firstOrNull();

		if (aiDocument == null) {
			final Document newAIDocument = Document.createDocument().put("setting", "aiFeatures")
					.put("enabled", false)
					.put("model", "gemma4:e2b");
			settingsCollection.insert(newAIDocument);
			return new AISettings(false, "gemma4:e2b");
		}

		final boolean enabled = Boolean.valueOf(aiDocument.get("enabled").toString());
		final String model = aiDocument.get("model").toString();

		return new AISettings(enabled, model);
	}

	public void setAISettings(AISettings settings) {
		System.out.printf("Setting AI settings: %s\n", settings.toString());

		final Document aiDocument = settingsCollection.find(where("setting").eq("aiFeatures"))
				.firstOrNull();

		if (aiDocument == null) {
			System.err.println("AI document is null.");
			final Document newAIDocument = Document.createDocument().put("setting", "aiFeatures")
					.put("enabled", settings.enabled())
					.put("model", settings.model());
			settingsCollection.insert(newAIDocument);
			return;
		}

		aiDocument.put("enabled", settings.enabled());
		aiDocument.put("model", settings.model());

		settingsCollection.update(aiDocument);
	}

	public record AISettings(boolean enabled, String model) {
	}
}
