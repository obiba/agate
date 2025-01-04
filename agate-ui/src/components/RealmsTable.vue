<template>
  <div>
    <q-table :rows="realms" flat row-key="name" :columns="columns" :pagination="initialPagination">
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
              <q-btn
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :title="t(props.row.status === 'ACTIVE' ? 'realm.deactivate' : 'realm.activate')"
                :icon="toolsVisible[props.row.name] ? (props.row.status === 'ACTIVE' ? 'pause' : 'play_arrow') : 'none'"
                class="q-ml-xs"
                @click="onToggleActivity(props.row)"
              />
            </div>
          </q-td>
          <q-td key="status" :props="props">
            <q-icon name="circle" size="sm" color="positive" v-if="props.row.status === 'ACTIVE'" />
            <q-icon name="circle" size="sm" color="warning" v-if="props.row.status === 'INACTIVE'" />
            <q-icon name="circle" size="sm" color="negative" v-if="props.row.status === 'UNRECOGNIZED'" />
          </q-td>
          <q-td key="type" :props="props">
            <code>{{ t(`realm.type.${props.row.type}`) }}</code>
          </q-td>
          <q-td key="userCount" :props="props">
            <q-badge :label="props.row.userCount" color="accent" />
          </q-td>
        </q-tr>
      </template>
    </q-table>
    <confirm-dialog
      v-model="showDelete"
      :title="t('realm.remove')"
      :text="t('realm.remove_confirm', { name: selected?.name })"
      @confirm="onDelete"
    />
  </div>
</template>

<script setup lang="ts">
import type { RealmConfigSummaryDto } from 'src/models/Agate';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import { DefaultAlignment } from 'src/components/models';

const { t } = useI18n();
const realmStore = useRealmStore();

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

const realms = computed(
  () =>
  realmStore.realms?.filter((rlm) =>
      filter.value ? rlm.name.toLowerCase().includes(filter.value.toLowerCase()) : true,
    ) || [],
);
const columns = computed(() => [
{ name: 'name', label: t('name'), field: 'name', align: DefaultAlignment },
  { name: 'status', label: t('status'), field: 'status', align: DefaultAlignment },
  { name: 'type', label: t('type'), field: 'type', align: DefaultAlignment },
  { name: 'userCount', label: t('realm.user_count'), field: 'userCount', align: DefaultAlignment },
]);

onMounted(() => {
  refresh();
});

function refresh() {
  realmStore.init();
}

function onOverRow(row: RealmConfigSummaryDto) {
  toolsVisible.value[row.name] = true;
}

function onLeaveRow(row: RealmConfigSummaryDto) {
  toolsVisible.value[row.name] = false;
}

function onShowEdit(row: RealmConfigSummaryDto) {
  selected.value = row;
  showEdit.value = true;
}

function onShowDelete(row: RealmConfigSummaryDto) {
  selected.value = row;
  showDelete.value = true;
}

function onDelete() {
  realmStore.remove(selected.value).finally(refresh);
}

function onAdd() {
  selected.value = undefined;
  showEdit.value = true;
}

function onToggleActivity(row: RealmConfigSummaryDto) {
  realmStore.toggleActivity(row).finally(refresh);
}
</script>
