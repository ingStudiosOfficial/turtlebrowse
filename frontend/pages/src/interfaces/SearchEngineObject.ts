import type { SearchEngine } from "@/types/SearchEngine";

export interface SearchEngineObject {
	engine: SearchEngine;
	template?: string | null;
}
