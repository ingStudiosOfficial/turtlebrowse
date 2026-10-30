import type { UserscriptMeta } from './UserscriptMeta';

export interface Userscript {
	meta: UserscriptMeta;
	metablock: string;
	content: string;
	filename: string;
}
