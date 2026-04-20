<script setup lang="ts">

</script>

<template>
  <v-app>
    <router-view />
  </v-app>
</template>

<style lang="scss">
.v-btn--size-small {
  font-size: 0.8125rem;
}
// See: https://github.com/vuetifyjs/vuetify/issues/16994
html {
  overflow-y: auto !important;
  font-family: 'Exo 2', sans-serif !important;
}

body {
  margin: 0;
  padding: 0;
}

#app {
  //font-family: Avenir, Helvetica, Arial, sans-serif;
  font-family: 'Exo 2', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

* {
  font-family: 'Exo 2', sans-serif !important;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>
