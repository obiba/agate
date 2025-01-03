<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('settings')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h6 q-mb-md">{{ t('properties') }}</div>
      <system-properties />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import SystemProperties from 'src/components/SystemProperties.vue';

const { t } = useI18n();
</script>
