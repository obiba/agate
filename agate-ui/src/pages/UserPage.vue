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
      <div class="row q-col-gutter-md">
        <div class="col-12 col-md-6">
          <div class="text-h6 q-mb-sm">{{ t('properties') }}</div>
          <div class="q-mb-md">
            <q-btn icon="edit" color="primary" size="sm" @click="onShowEdit" />
            <q-btn outline icon="delete" color="negative" size="sm" class="on-right" @click="onShowDelete" />
          </div>
          <fields-list :dbobject="user" :items="items" />
        </div>
        <div class="col-12 col-md-6">
        </div>
      </div>
      <confirm-dialog v-model="showDelete" :title="t('user.remove')"
        :text="t('user.remove_confirm', { name: selected?.name })" @confirm="onDelete" />
      <user-dialog v-model="showEdit" :user="selected" @saved="onSaved" />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import type { UserDto } from 'src/models/Agate';
import type { FieldItem } from 'src/components/FieldsList.vue';
import FieldsList from 'src/components/FieldsList.vue';
import UserDialog from 'src/components/UserDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import { getDateLabel } from 'src/utils/dates';

const { t } = useI18n();
const router = useRouter();
const userStore = useUserStore();
const groupStore = useGroupStore();
const applicationStore = useApplicationStore();

const id = computed(() => router.currentRoute.value.params.id as string);
const user = computed(() => userStore.getUser(id.value));
const name = computed(() => user.value?.name || id.value);

const showEdit = ref(false);
const showDelete = ref(false);
const selected = ref<UserDto>();

const items: FieldItem[] = [
  {
    field: 'name',
  },
  {
    field: 'email',
  },
  {
    field: 'firstName',
    label: t('fullName'),
    format: (val: UserDto) => `${val.firstName} ${val.lastName}`,
  },
  {
    field: 'preferredLanguage',
    label: t('language'),
  },
  {
    field: 'role',
    format: (val: UserDto) => t(`user.role.${val.role}`),
  },
  {
    field: 'status',
    format: (val: UserDto) => t(`user.status.${val.status}`),
  },
  {
    field: 'realm',
  },
  {
    field: 'otpEnabled',
    icon: (val: UserDto) => (val.otpEnabled ? 'check' : 'close'),
  },
  {
    field: 'groups',
    links: (val: UserDto) =>
      val.groups?.map((name) => ({
        label: groupStore.getGroupName(name),
        to: `/group/${name}`,
      })) || [],
    //format: (val: UserDto) => val.groups?.join(', '),
  },
  {
    field: 'applications',
    links: (val: UserDto) =>
      val.applications?.map((name) => ({
        label: applicationStore.getApplicationName(name),
        to: `/application/${name}`,
      })) || [],
    //format: (val: UserDto) => val.applications?.join(', ') + ' ' + (val.groupApplications?.length ? `(${val.groupApplications.map(ga => ga.application).join(', ')})` : ''),
  },
  {
    field: 'attributes',
    html: (val: UserDto) =>
      val.attributes?.map((attr) => `<code>${attr.name}: ${attr.value}</code>`).join(' '),
  },
  {
    field: 'lastModified',
    label: t('last_modified'),
    format: (val: UserDto) => (val ? getDateLabel(val.timestamps?.lastUpdate ? val.timestamps?.lastUpdate : val.timestamps?.created) : ''),
  },
]

onMounted(() => {
  userStore.init();
  groupStore.init();
  applicationStore.init();
});

function onShowEdit() {
  selected.value = JSON.parse(JSON.stringify(user.value));
  showEdit.value = true;
}

function onShowDelete() {
  selected.value = user.value;
  showDelete.value = true;
}

function onDelete() {
  if (!selected.value) {
    return;
  }
  userStore.remove(selected.value).finally(() => {
    router.push('/users');
  });
}

function onSaved() {
  userStore.init();
}
</script>
