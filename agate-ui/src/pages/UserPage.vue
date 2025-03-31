<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('users')" to="/users" />
        <q-breadcrumbs-el :label="name" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <pre>{{ user }}</pre>
    </q-page>
  </div>
</template>

<script setup lang="ts">
const { t } = useI18n();
const router = useRouter();
const userStore = useUserStore();

const id = computed(() => router.currentRoute.value.params.id as string);
const user = computed(() => userStore.getUser(id.value));
const name = computed(() => user.value?.name || id.value);

onMounted(() => {
  userStore.init();
});

</script>
