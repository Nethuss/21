<template>
  <div class="d-flex flex-column">
    <span class="text-caption text-medium-emphasis mb-1">
      {{ label }}
    </span>

    <div class="d-flex align-center text-subtitle-2">
      <template v-if="chip">
        <v-chip
          :color="color"
          variant="flat"
          class="px-3"
          rounded="pill"
        >
          <v-icon v-if="icon" class="me-2" :icon="icon" />
          <strong>{{ value }}</strong>
        </v-chip>
      </template>

      <template v-else>
        <strong>{{ value }}</strong>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps({
  label: String,
  value: String,
  icon: String,
  chip: Boolean,
  color: String,
});
</script>
