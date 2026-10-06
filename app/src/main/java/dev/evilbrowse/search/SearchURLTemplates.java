package dev.evilbrowse.search;

import java.util.HashMap;
import java.util.Map;

public class SearchURLTemplates {
	public static Map<String, String> searchTemplates = new HashMap<>();

	static {
		searchTemplates.put("brave", "https://search.brave.com/search?q=%s");
		searchTemplates.put("ddg", "https://duckduckgo.com/?q=%s");
		searchTemplates.put("ddg-noai", "https://noai.duckduckgo.com/?q=%s");
		searchTemplates.put("google", "https://google.com/search?q=%s");
		searchTemplates.put("startpage", "https://startpage.com/sp/search?query=%s");
		searchTemplates.put("yahoo", "https://search.yahoo.com/search?p=%s");
		searchTemplates.put("vyntr", "https://vyntr.com/search?q=%s");
		searchTemplates.put("bing", "https://bing.com/search?q=%s");
		// Aliases referenced by the frontend settings UI. Without these,
		// selecting them yields a null template and a later NPE on formatted().
		searchTemplates.put("kagi", "https://kagi.com/search?q=%s");
		searchTemplates.put("custom", "https://search.brave.com/search?q=%s");
	}

	public static String getTemplate(String engine) {
		if (engine == null) {
			return searchTemplates.get("brave");
		}
		String template = searchTemplates.get(engine);
		if (template == null) {
			template = searchTemplates.get("brave");
		}
		return template;
	}
}
