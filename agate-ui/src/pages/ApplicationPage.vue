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
          <fields-list :dbobject="application" :items="items" />
        </div>
        <div class="col-12 col-md-6">
        </div>
      </div>
      <confirm-dialog
        v-model="showDelete"
        :title="t('application.remove')"
        :text="t('application.remove_confirm', { name: selected?.name })"
        @confirm="onDelete"
      />
      <application-dialog v-model="showEdit" :application="selected" @saved="onSaved" />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import type { FieldItem } from 'src/components/FieldsList.vue';
import ApplicationDialog from 'src/components/ApplicationDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import FieldsList from 'src/components/FieldsList.vue';
import { getDateLabel } from 'src/utils/dates';
import type { ApplicationDto } from 'src/models/Agate';

const { t } = useI18n();
const router = useRouter();
const applicationStore = useApplicationStore();

const id = computed(() => router.currentRoute.value.params.id as string);
const application = computed(() => applicationStore.getApplication(id.value));
const name = computed(() => application.value?.name || id.value);

const showEdit = ref(false);
const showDelete = ref(false);
const selected = ref<ApplicationDto>();

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
    field: 'redirectURI',
    label: t('application.redirect_uris'),
    links: (val: ApplicationDto) =>
    val.redirectURI?.split(',').map((uri) => {
      return {
        label: uri,
        to: uri,
        iconRight: 'open_in_new',
      }}) || [],
  },
  {
    field: 'autoApproval',
    label: t('application.auto_approval'),
    icon: (val: ApplicationDto) => (val.autoApproval ? 'check' : 'close'),
  },
  {
    field: 'scopes',
    label: t('application.scopes'),
    html: (val: ApplicationDto) => {
      if (!val.scopes) {
        return '-';
      }
      return val.scopes.map((scope) => `<span class="text-caption" title="${scope.description}">${scope.name}</span>`).join(' | ');
    },
  },
  {
    field: 'realmGroups',
    label: t('application.realms_groups'),
    html: (val: ApplicationDto) => {
      if (!val.realmGroups) {
        return '-';
      }
      return val.realmGroups.map((rg) => `<span class="text-caption" title="${rg.groups.join(', ')}">${rg.realm}</span>`).join(' | ');
    },
  },
  {
    field: 'lastModified',
    label: t('last_modified'),
    format: (val: ApplicationDto) => (val ? getDateLabel(val.timestamps?.lastUpdate ? val.timestamps?.lastUpdate : val.timestamps?.created) : ''),
  },
];

onMounted(() => {
  applicationStore.init();
});

function onShowEdit() {
  selected.value = JSON.parse(JSON.stringify(application.value));
  showEdit.value = true;
}

function onSaved() {
  applicationStore.init();
}

function onShowDelete() {
  selected.value = application.value;
  showDelete.value = true;
}

function onDelete() {
  if (!selected.value) {
    return;
  }
  applicationStore.remove(selected.value).finally(() => {
    router.push('/applications');
  });
}

</script>
