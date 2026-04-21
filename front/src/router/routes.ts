import type { RouteRecordRaw } from "vue-router";
import { routesLogin } from "@/router/login/routesLogin";
import MainLayout from "@/view/MainLayout.vue";

export const routes: Array<RouteRecordRaw> = [
    {path: "/",
    component: MainLayout,
        name: "main",
    children: [
    {
        path: "groups",
        name: "groups",
        component: () => import("@/view/GroupsView.vue"),
    },
    {
        path: "subjects",
        name: "subjects",
        component: () => import("@/view/SubjectsView.vue"),
    },
    {
        path: "users",
        name: "users",
        component: () => import("@/view/UsersView.vue"),
    },
    {
        path: "schedule",
        name: "Расписание",
        component: () => import("@/view/ScheduleView.vue"),
    },
],
    },
    routesLogin,
];
