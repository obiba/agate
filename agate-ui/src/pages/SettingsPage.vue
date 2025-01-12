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
      <system-user-attributes class="q-mt-md"/>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import SystemProperties from 'src/components/SystemProperties.vue';
import SystemUserAttributes from 'src/components/SystemUserAttributes.vue';


const systemStore = useSystemStore();
const { t } = useI18n();

onMounted(() => {
  systemStore.init();
});

</script>
