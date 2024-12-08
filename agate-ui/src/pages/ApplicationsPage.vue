<template>
<div>
    <q-toolbar class="bg-grey-3">
    <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('applications')" />
    </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
        <pre>{{ applicationStore.applications }}</pre>
    </q-page>
</div>
</template>

<script setup lang="ts">
const applicationStore = useApplicationStore();

onMounted(() => {
    applicationStore.init();
});
</script>
