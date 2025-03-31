<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('groups')" to="/groups" />
        <q-breadcrumbs-el :label="name" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <pre>{{ group }}</pre>
      <pre>{{ users }}</pre>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import type { UserSummaryDto } from 'src/models/Agate';


const { t } = useI18n();
const router = useRouter();
const groupStore = useGroupStore();

const id = computed(() => router.currentRoute.value.params.id as string);
const group = computed(() => groupStore.getGroup(id.value));
const name = computed(() => group.value?.name || id.value);
const users = ref<UserSummaryDto[]>([]);

onMounted(() => {
  groupStore.init();
  groupStore.getUsers(id.value).then((data) => {
    users.value = data;
  });
});

</script>
