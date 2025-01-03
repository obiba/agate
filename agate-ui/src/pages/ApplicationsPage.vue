<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('applications')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <applications-table />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import ApplicationsTable from 'src/components/ApplicationsTable.vue';

const { t } = useI18n();
</script>
