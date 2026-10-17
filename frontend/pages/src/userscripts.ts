import { createApp } from 'vue';
import UserscriptsApp from './UserscriptsApp.vue';
import '@/assets/main.css';
import 'material-symbols/outlined.css';
import { setTheme } from './utils/theme.ts';

const app = createApp(UserscriptsApp);

app.mount('#app');

setTheme();
