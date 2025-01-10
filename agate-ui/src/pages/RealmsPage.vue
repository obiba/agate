<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('realms')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <realms-table />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import RealmsTable from 'src/components/RealmsTable.vue';
const { t } = useI18n();
</script>
