<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('tickets')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <tickets-table />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import TicketsTable from 'src/components/TicketsTable.vue';

const { t } = useI18n();
</script>
