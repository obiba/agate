<template>
  <div>
    <q-table :rows="tickets" flat row-key="name" :columns="columns" :pagination="initialPagination">
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
            <span class="text-primary">{{ props.row.id }}</span>
            <div class="float-right">
              <q-btn
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :title="t('delete')"
                :icon="toolsVisible[props.row.id] ? 'delete' : 'none'"
                class="q-ml-xs"
                @click="onShowDelete(props.row)"
              />
            </div>
          </q-td>
          <q-td key="username" :props="props">
            <q-chip size="sm">{{ props.row.username }}</q-chip>
          </q-td>
          <q-td key="expires" :props="props">
            <span>{{ getDateLabel(props.row.expires) }}</span>
          </q-td>
          <q-td key="login_app" :props="props">
            <q-badge :label="applicationStore.getApplicationName(getLoginApp(props.row))" />
          </q-td>
          <q-td key="events" :props="props">
            <q-btn :label="props.row.events?.length ?? 0" color="accent" size="sm" @click="onShowEvents(props.row)" />
          </q-td>
        </q-tr>
      </template>
    </q-table>
    <confirm-dialog
      v-model="showDelete"
      :title="t('ticket.remove')"
      :text="t('ticket.remove_confirm', { id: selected?.id, username: selected?.username })"
      @confirm="onDelete"
    />
    <ticket-events-dialog v-model="showEventsDialog" :ticket="selected" />
  </div>
</template>

<script setup lang="ts">
import type { TicketDto } from 'src/models/Agate';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import TicketEventsDialog from 'src/components/TicketEventsDialog.vue';
import { DefaultAlignment } from 'src/components/models';
import { getDateLabel } from 'src/utils/dates';

const { t } = useI18n();
const ticketStore = useTicketStore();
const applicationStore = useApplicationStore();

const filter = ref('');
const toolsVisible = ref<{ [key: string]: boolean }>({});
const initialPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 20,
});
const showDelete = ref(false);
const showEventsDialog = ref(false);
const selected = ref();

const tickets = computed<TicketDto[]>(
  () =>
    ticketStore.tickets?.filter((tk) =>
      filter.value ? tk.username.toLowerCase().includes(filter.value.toLowerCase()) : true,
    ) || [],
);
const columns = computed(() => [
  { name: 'id', label: 'ID', field: 'id', align: DefaultAlignment, sortable: true },
  { name: 'username', label: t('username'), field: 'username', align: DefaultAlignment, sortable: true },
  { name: 'expires', label: t('ticket.expires'), field: 'expires', align: DefaultAlignment, sortable: true },
  { name: 'login_app', label: t('ticket.login_app'), field: 'events', align: DefaultAlignment, sortable: true },
  { name: 'events', label: t('ticket.events'), field: 'events', align: DefaultAlignment, sortable: false },
]);

onMounted(() => {
  applicationStore.init();
  refresh();
});

function refresh() {
  ticketStore.init();
}

function onOverRow(row: TicketDto) {
  toolsVisible.value[row.id] = true;
}

function onLeaveRow(row: TicketDto) {
  toolsVisible.value[row.id] = false;
}

function onShowDelete(row: TicketDto) {
  selected.value = row;
  showDelete.value = true;
}

function onShowEvents(row: TicketDto) {
  selected.value = row;
  showEventsDialog.value = true;
}

function onDelete() {
  ticketStore.remove(selected.value).finally(refresh);
}

function getLoginApp(row: TicketDto) {
  return row.events?.find((evt) => evt.action === 'login')?.application;
}
</script>
