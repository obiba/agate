<template>
  <q-dialog v-model="showDialog" persistent @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t('ticket.events') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-list separator class="fields-list">
          <q-item v-for="(event, idx) in events" :key="idx">
            <q-item-section>
              <q-item-label>
                <q-badge :label="applicationStore.getApplicationName(event.application)" />
              </q-item-label>
              <q-item-label caption>{{ getDateLabel(event.time) }}</q-item-label>
            </q-item-section>
            <q-item-section avatar>
              <q-chip :label="event.action" size="sm" color="accent" class="text-white" />
            </q-item-section>
          </q-item>
        </q-list>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('close')" color="primary" v-close-popup />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { TicketDto } from 'src/models/Agate';
import { getDateLabel } from 'src/utils/dates';

const { t } = useI18n();
const applicationStore = useApplicationStore();

interface DialogProps {
  modelValue: boolean;
  ticket: TicketDto | undefined;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue']);

const showDialog = ref(props.modelValue);
const events = ref(props.ticket?.events);

watch(
  () => props.modelValue,
  (value) => {
    showDialog.value = value;
    events.value = props.ticket?.events;
  },
);

function onHide() {
  emit('update:modelValue', false);
}
</script>
