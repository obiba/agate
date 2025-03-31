<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('applications')" to="/applications" />
        <q-breadcrumbs-el :label="name" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <pre>{{ application }}</pre>
    </q-page>
  </div>
</template>

<script setup lang="ts">
const { t } = useI18n();
const router = useRouter();
const applicationStore = useApplicationStore();

const id = computed(() => router.currentRoute.value.params.id as string);
const application = computed(() => applicationStore.getApplication(id.value));
const name = computed(() => application.value?.name || id.value);

onMounted(() => {
  applicationStore.init();
});

</script>
