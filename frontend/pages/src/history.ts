import { createApp } from 'vue';
import HistoryApp from './HistoryApp.vue';
import '@/assets/main.css';
import 'material-symbols/outlined.css';
import { setTheme } from './utils/theme.ts';

const app = createApp(HistoryApp);

app.mount('#app');

setTheme();
