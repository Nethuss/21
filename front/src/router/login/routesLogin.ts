import { RouteRecordRaw } from "vue-router";
import UserLoginView from "@/view/UserLoginView.vue";

export const routesLogin: RouteRecordRaw =  {
    path: "/login",
    component: UserLoginView,
    name: "login",
  }
;
