<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('realms')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <pre>{{ realmStore.realms }}</pre>
    </q-page>
  </div>
</template>

<script setup lang="ts">
const { t } = useI18n();
const realmStore = useRealmStore();

onMounted(() => {
  realmStore.init();
});
</script>
