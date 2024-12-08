<template>
<div>
    <q-toolbar class="bg-grey-3">
    <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('groups')" />
    </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
        <pre>{{ groupStore.groups }}</pre>
    </q-page>
</div>
</template>

<script setup lang="ts">
const groupStore = useGroupStore();

onMounted(() => {
    groupStore.init();
});
</script>
