import type { RouteRecordRaw } from "vue-router";
import { routesLogin } from "@/router/login/routesLogin";

export const routes: Array<RouteRecordRaw> = [
    {
        path: "/",
        redirect: "/login",
    },
    routesLogin,
];
