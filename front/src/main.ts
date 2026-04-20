// src/main.ts
import { createPinia } from "pinia";
import piniaPersist from "pinia-plugin-persistedstate";
import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import dayjs from "dayjs";
import ru from "dayjs/locale/ru";
import timezone from "dayjs/plugin/timezone";
import utc from "dayjs/plugin/utc";
import "@/sass/main.scss";

import "@fontsource/exo-2/latin-400.css";
import "@fontsource/exo-2/cyrillic-400.css";
import "@fontsource/exo-2/latin-600.css";
import "@fontsource/exo-2/cyrillic-600.css";
import "@fontsource/exo-2/latin-700.css";
import "@fontsource/exo-2/cyrillic-700.css";
import "@fontsource/exo-2/latin-900.css";
import "@fontsource/exo-2/cyrillic-900.css";

dayjs.locale(ru);
dayjs.extend(utc);
dayjs.extend(timezone);
dayjs.tz.guess();

import vuetify from "@/config/vuetify";
import "@/sass/variables.scss";

const pinia = createPinia();
pinia.use(piniaPersist);

const app = createApp(App).use(pinia).use(vuetify);
app.use(router).mount("#app");