<template>
  <div>
    <q-table :rows="applications" flat row-key="name" :columns="columns" :pagination="initialPagination">
      <template v-slot:top-left>
        <q-btn size="sm" icon="add" color="primary" :label="t('add')" @click="onAdd" />
      </template>
      <template v-slot:top-right>
        <q-input v-model="filter" debounce="300" :placeholder="t('search')" dense clearable class="q-mr-md">
          <template v-slot:prepend>
            <q-icon name="search" />
          </template>
        </q-input>
      </template>
      <template v-slot:body="props">
        <q-tr :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <q-td key="id" :props="props">
            <code>{{ props.row.id }}</code>
          </q-td>
          <q-td key="name" :props="props">
            <router-link :to="`/application/${props.row.id}`">{{ props.row.name }}</router-link>
            <div class="float-right">
              <q-btn
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :icon="toolsVisible[props.row.name] ? 'edit' : 'none'"
                :title="t('edit')"
                class="q-ml-xs"
                @click="onShowEdit(props.row)"
              />
              <q-btn
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :title="t('delete')"
                :icon="toolsVisible[props.row.name] ? 'delete' : 'none'"
                class="q-ml-xs"
                @click="onShowDelete(props.row)"
              />
            </div>
          </q-td>
          <q-td key="description" :props="props">
            <span>{{ props.row.description }}</span>
          </q-td>
          <q-td key="scopes" :props="props">
            <template v-for="scope in props.row.scopes?.sort()" :key="scope.name">
              <q-badge :label="scope.name" :title="scope.description" class="on-left" />
            </template>
          </q-td>
          <q-td key="realmGroups" :props="props">
            <template v-for="realmGroups in props.row.realmGroups?.sort()" :key="realmGroups.realm">
              <q-badge :label="realmGroups.realm" :title="realmGroups.groups.join(', ')" class="on-left" />
            </template>
          </q-td>
        </q-tr>
      </template>
    </q-table>
    <confirm-dialog
      v-model="showDelete"
      :title="t('application.remove')"
      :text="t('application.remove_confirm', { name: selected?.name })"
      @confirm="onDelete"
    />
    <application-dialog v-model="showEdit" :application="selected" @saved="onSaved" />
  </div>
</template>

<script setup lang="ts">
import type { ApplicationDto } from 'src/models/Agate';
import ApplicationDialog from 'src/components/ApplicationDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import { DefaultAlignment } from 'src/components/models';

const { t } = useI18n();
const applicationStore = useApplicationStore();

const filter = ref('');
const toolsVisible = ref<{ [key: string]: boolean }>({});
const initialPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 20,
});
const showEdit = ref(false);
const showDelete = ref(false);
const selected = ref();

const applications = computed(
  () =>
    applicationStore.applications?.filter((app) =>
      filter.value ? app.name.toLowerCase().includes(filter.value.toLowerCase()) : true,
    ) || [],
);
const columns = computed(() => [
  { name: 'id', label: 'ID', field: 'id', align: DefaultAlignment, sortable: true },
  { name: 'name', label: t('name'), field: 'name', align: DefaultAlignment, sortable: true },
  { name: 'description', label: t('description'), field: 'description', align: DefaultAlignment, sortable: true },
  { name: 'scopes', label: t('application.scopes'), field: 'scopes', align: DefaultAlignment, sortable: true },
  {
    name: 'realmGroups',
    label: t('application.realms_groups'),
    field: 'realmGroups',
    align: DefaultAlignment,
    sortable: true,
  },
]);

onMounted(() => {
  refresh();
});

function refresh() {
  applicationStore.init();
}

function onOverRow(row: ApplicationDto) {
  toolsVisible.value[row.name] = true;
}

function onLeaveRow(row: ApplicationDto) {
  toolsVisible.value[row.name] = false;
}

function onShowEdit(row: ApplicationDto) {
  selected.value = row;
  showEdit.value = true;
}

function onShowDelete(row: ApplicationDto) {
  selected.value = row;
  showDelete.value = true;
}

function onDelete() {
  applicationStore.remove(selected.value).finally(refresh);
}

function onAdd() {
  selected.value = undefined;
  showEdit.value = true;
}

function onSaved() {
  refresh();
}
</script>
