<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('users')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <users-table />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import UsersTable from 'src/components/UsersTable.vue';
const { t } = useI18n();
</script>
