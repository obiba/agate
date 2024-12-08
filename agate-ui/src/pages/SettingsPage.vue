<template>
<div>
    <q-toolbar class="bg-grey-3">
    <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('settings')" />
    </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
        <pre>{{ systemStore.configuration }}</pre>
    </q-page>
</div>
</template>

<script setup lang="ts">
const systemStore = useSystemStore();

onMounted(() => {
    systemStore.init();
});
</script>
