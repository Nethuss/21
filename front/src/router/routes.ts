import type { RouteRecordRaw } from "vue-router";
import { routesLogin } from "@/router/login/routesLogin";
import AdminView from "@/view/AdminView.vue";

export const routes: Array<RouteRecordRaw> = [
    {
        path: "/",
        redirect: "/login",
    },
    routesLogin,
    {
        path: "/admin",
        component: AdminView,
        name: "admin"
    }
];
