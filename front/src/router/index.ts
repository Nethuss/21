import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "@/store/useAuthStore";
import { routes } from "./routes";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

router.beforeEach((to) => {

  const lastMatched = to.matched[to.matched.length - 1];
  const needsAuth = lastMatched.meta.requiresAuth === true;

  if (needsAuth) {
    const authStore = useAuthStore();
    const userRole = authStore.role;

    const allowedRoles = lastMatched.meta.roles as string[] | undefined;
    if (allowedRoles && !allowedRoles.includes(userRole)) {
      return {name: "login"};
    }
  }

  return true;
});

export default router;
