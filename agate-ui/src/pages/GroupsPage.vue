<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('groups')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <groups-table />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import GroupsTable from 'src/components/GroupsTable.vue';

const { t } = useI18n();
</script>
