import {createVuetify} from "vuetify";
import {mainThemeDark, mainThemeLight} from "@/config/theme";
import {aliases, mdi} from "vuetify/iconsets/mdi-svg";
import {ru} from "vuetify/locale";
import "vuetify/styles";
import DayJsAdapter from '@date-io/dayjs'
import enUS from "dayjs/locale/en.js";
import ruRU from "dayjs/locale/ru.js";
// 👇 ДОБАВЬ ЭТО
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

export default createVuetify({
    components,
    directives,
    theme: {
        themes: {
            mainThemeLight,
            mainThemeDark
        },
    },
    icons: {
        defaultSet: "mdi",
        aliases,
        sets: {
            mdi,
        },
    },
    locale: {
        locale: 'ru',
        messages: { ru },
    },
    date: {
        adapter: DayJsAdapter,
        locale: {
            en: enUS,
            ru: ruRU,
        },
    },
    defaults: {
        VBtn: {
            color: 'primary',
            variant: 'tonal',
            class: ['round-button']
        },
        VTextField: {
            variant: "underlined",
            color: "primary"
        }
    }
});
