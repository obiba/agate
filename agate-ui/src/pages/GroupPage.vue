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
      <div class="row q-col-gutter-md">
        <div class="col-12 col-md-6">
          <div class="text-h6 q-mb-sm">{{ t('properties') }}</div>
          <div class="q-mb-md">
            <q-btn
              icon="edit"
              color="primary"
              size="sm"
              @click="onShowEdit"
            />
            <q-btn
              outline
              icon="delete"
              color="negative"
              size="sm"
              class="on-right"
              @click="onShowDelete"
            />
          </div>
          <fields-list :dbobject="group" :items="items" />
        </div>
        <div class="col-12 col-md-6">
          <div class="text-h6 q-mb-sm">{{ t('users') }}</div>
          <q-list bordered separator>
            <q-item v-for="user in users" :key="user.name" clickable>
              <q-item-section>
                <q-item-label>
                  <div class="text-bold">{{ user.name }}</div>
                  <div class="text-caption">{{ user.firstName }} {{ user.lastName }}</div>
                  <div class="text-hint">{{ user.email }}</div>
                </q-item-label>
              </q-item-section>
              <q-item-section side>
                <q-btn
                  flat
                  icon="delete"
                  color="negative"
                  size="sm"
                  @click="onShowRemoveUser(user)"
                />
              </q-item-section>
            </q-item>
          </q-list>
        </div>
      </div>
      <confirm-dialog
        v-model="showDelete"
        :title="t('group.remove')"
        :text="t('group.remove_confirm', { name: selected?.name })"
        @confirm="onDelete"
      />
      <group-dialog v-model="showEdit" :group="selected" @saved="onSaved" />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import type { GroupDto, UserSummaryDto } from 'src/models/Agate';
import type { FieldItem } from 'src/components/FieldsList.vue';
import GroupDialog from 'src/components/GroupDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import FieldsList from 'src/components/FieldsList.vue';
import { getDateLabel } from 'src/utils/dates';


const { t } = useI18n();
const router = useRouter();
const groupStore = useGroupStore();
const applicationStore = useApplicationStore();

const id = computed(() => router.currentRoute.value.params.id as string);
const group = computed(() => groupStore.getGroup(id.value));
const name = computed(() => group.value?.name || id.value);
const users = ref<UserSummaryDto[]>([]);

const showEdit = ref(false);
const showDelete = ref(false);
const selected = ref<GroupDto>();

const items: FieldItem[] = [
  {
    field: 'id',
    label: 'ID',
    html: (val) => (val ? `<code>${val.id}</code>` : ''),
  },
  {
    field: 'name',
  },
  {
    field: 'description',
  },
  {
    field: 'applications',
    label: t('applications'),
    //format: (val: GroupDto) => (val ? val.applications?.map((name) => applicationStore.getApplicationName(name)).join(', ') : ''),
    links: (val: GroupDto) =>
      val.applications?.map((name) => ({
        label: applicationStore.getApplicationName(name),
        to: `/application/${name}`,
      })) || [],
  },
  {
    field: 'lastModified',
    label: t('last_modified'),
    format: (val: GroupDto) => (val ? getDateLabel(val.timestamps?.lastUpdate ? val.timestamps?.lastUpdate : val.timestamps?.created) : ''),
  },
];

onMounted(() => {
  groupStore.init();
  applicationStore.init();
  groupStore.getUsers(id.value).then((data) => {
    users.value = data;
  });
});

function onShowEdit() {
  selected.value = JSON.parse(JSON.stringify(group.value));
  showEdit.value = true;
}

function onSaved() {
  groupStore.init();
}

function onShowDelete() {
  selected.value = group.value;
  showDelete.value = true;
}

function onDelete() {
  if (!selected.value) {
    return;
  }
  groupStore.remove(selected.value).finally(() => {
    router.push('/groups');
  });
}

function onShowRemoveUser(user: UserSummaryDto) {
  // TODO
  console.log('Remove user', user);
}
</script>
