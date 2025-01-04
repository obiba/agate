<template>
  <div>
    <q-table :rows="groups" flat row-key="name" :columns="columns" :pagination="initialPagination">
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
          <q-td key="name" :props="props">
            <span class="text-primary">{{ props.row.name }}</span>
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
                v-if="!props.row.hasDatasource"
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
          <q-td key="applications" :props="props">
            <template v-for="app in props.row.applications" :key="app">
              <q-badge :label="applicationStore.getApplicationName(app)" class="on-left" />
            </template>
          </q-td>
        </q-tr>
      </template>
    </q-table>
    <confirm-dialog
      v-model="showDelete"
      :title="t('group.remove')"
      :text="t('group.remove_confirm', { name: selected?.name })"
      @confirm="onDelete"
    />
    <group-dialog v-model="showEdit" :group="selected" @saved="onSaved" />
  </div>
</template>

<script setup lang="ts">
import type { GroupDto } from 'src/models/Agate';
import GroupDialog from 'src/components/GroupDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import { DefaultAlignment } from 'src/components/models';

const { t } = useI18n();
const groupStore = useGroupStore();
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

const groups = computed(
  () =>
    groupStore.groups?.filter((grp) =>
      filter.value ? grp.name.toLowerCase().includes(filter.value.toLowerCase()) : true,
    ) || [],
);
const columns = computed(() => [
  { name: 'name', label: t('name'), field: 'name', align: DefaultAlignment },
  { name: 'description', label: t('description'), field: 'description', align: DefaultAlignment },
  { name: 'applications', label: t('applications'), field: 'applications', align: DefaultAlignment },
]);

onMounted(() => {
  applicationStore.init();
  refresh();
});

function refresh() {
  groupStore.init();
}

function onOverRow(row: GroupDto) {
  toolsVisible.value[row.name] = true;
}

function onLeaveRow(row: GroupDto) {
  toolsVisible.value[row.name] = false;
}

function onShowEdit(row: GroupDto) {
  selected.value = row;
  showEdit.value = true;
}

function onShowDelete(row: GroupDto) {
  selected.value = row;
  showDelete.value = true;
}

function onDelete() {
  groupStore.remove(selected.value).finally(refresh);
}

function onAdd() {
  selected.value = undefined;
  showEdit.value = true;
}

function onSaved() {
  refresh();
}
</script>
