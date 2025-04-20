<template>
  <q-dialog v-model="showDialog" persistent @hide="onHide">
    <q-card class="dialog-md">
      <q-card-section>
        <div class="text-h6">{{ t(editMode ? 'group.edit' : 'group.add') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-form>
          <q-input
            v-model="selected.name"
            :label="t('name') + ' *'"
            :hint="t('name_hint')"
            :disable="editMode"
            dense
            lazy-rules
            :rules="[
              (val) => !!val || t('name_required'),
              (val) => (val?.trim().length ?? 0) >= 3 || t('name_min_length', { min: 3 }),
            ]"
          />
          <q-input v-model="selected.description" :label="t('description')" dense class="q-mb-md" />
          <q-select
            v-model="selected.applications"
            :label="t('applications')"
            :hint="t('group.applications_hint')"
            :options="applicationOptions"
            dense
            emit-value
            map-options
            use-chips
            multiple
            class="q-mb-md"
          />
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" @click="onCancel" v-close-popup />
        <q-btn flat :label="t('save')" :disable="!isValid" color="primary" @click="onSave" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { GroupDto } from 'src/models/Agate';
import { notifyError, notifySuccess } from 'src/utils/notify';

const { t } = useI18n();
const groupStore = useGroupStore();
const applicationStore = useApplicationStore();

interface DialogProps {
  modelValue: boolean;
  group: GroupDto | undefined;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'saved', 'cancel']);

const showDialog = ref(props.modelValue);
const selected = ref<GroupDto>(props.group ?? ({} as GroupDto));
const editMode = ref(false);

const applicationOptions = computed(
  () => (applicationStore.applications?.map((app) => ({ label: app.name, value: app.id })) ?? []).sort((a, b) => a.label.localeCompare(b.label)),
);
const isValid = computed(() => selected.value.name && selected.value.name.trim().length >= 3);

watch(
  () => props.modelValue,
  (value) => {
    showDialog.value = value;
    selected.value = props.group ? { ...props.group } : ({} as GroupDto);
    editMode.value = props.group !== undefined;
  },
);

function onHide() {
  emit('update:modelValue', false);
}

function onCancel() {
  emit('cancel');
}

function onSave() {
  groupStore
    .save(selected.value)
    .then(() => {
      notifySuccess(t('group.saved'));
      emit('saved');
      emit('update:modelValue', false);
    })
    .catch((err) => {
      notifyError(err);
    });
}
</script>
