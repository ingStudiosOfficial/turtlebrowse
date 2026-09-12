import { applyTheme, argbFromHex, themeFromSourceColor } from '@material/material-color-utilities';
import { fetchFromJavaText } from './java_bridge';

export async function setTheme() {
	const seedColor = await fetchFromJavaText('GET_THEME');
	console.log('Seed color:', seedColor);
	if (!seedColor) return;
	const theme = themeFromSourceColor(argbFromHex(seedColor));
	const systemDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
	applyTheme(theme, { target: document.body, dark: systemDark });
}
