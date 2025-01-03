<template>
  <q-dialog v-model="showDialog" persistent @hide="onHide">
    <q-card class="dialog-md">
      <q-card-section>
        <div class="text-h6">{{ t(editMode ? 'application.edit' : 'application.add') }}</div>
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
          <q-input
            v-model="key"
            :label="t('application.key') + (editMode ? '' : ' *')"
            :hint="t(editMode ? 'application.key_hint_edit' : 'application.key_hint')"
            dense
            lazy-rules
            :rules="
              editMode
                ? []
                : [
                    (val) => !!val || t('application.key_required'),
                    (val) => (val?.trim().length ?? 0) >= 8 || t('application.key_min_length', { min: 8 }),
                  ]
            "
            class="q-mb-md"
          >
            <template v-slot:after>
              <q-btn
                round
                dense
                size="sm"
                :title="t('application.copy_key')"
                flat
                icon="content_copy"
                @click="copyKeyToClipboard"
              />
              <q-btn
                round
                dense
                size="sm"
                :title="t('application.generate_key')"
                flat
                icon="lock_reset"
                @click="generateKey"
              />
            </template>
          </q-input>
          <q-input
            v-model="selected.redirectURI"
            :label="t('application.redirect_uris')"
            :hint="t('application.redirect_uris_hint')"
            dense
            class="q-mb-md"
          />
          <q-checkbox v-model="selected.autoApproval" :label="t('application.auto_approval')" dense class="q-mb-xs" />
          <div class="text-hint q-mb-md">
            {{ t('application.auto_approval_hint') }}
          </div>
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
import type { ApplicationDto } from 'src/models/Agate';
import { notifyError, notifyInfo, notifySuccess } from 'src/utils/notify';
import { copyToClipboard } from 'quasar';

const { t } = useI18n();
const applicationStore = useApplicationStore();

interface DialogProps {
  modelValue: boolean;
  application: ApplicationDto | undefined;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'saved', 'cancel']);

const showDialog = ref(props.modelValue);
const selected = ref<ApplicationDto>(props.application ?? ({ autoApproval: true } as ApplicationDto));
const editMode = ref(false);
const key = ref('');

const isValid = computed(
  () =>
    selected.value.name &&
    selected.value.name.trim().length >= 3 &&
    (editMode || (key.value && key.value.trim().length >= 8)),
);

onMounted(() => {
  applicationStore.init();
});

watch(
  () => props.modelValue,
  (value) => {
    showDialog.value = value;
    selected.value = props.application ? { ...props.application } : ({ autoApproval: true } as ApplicationDto);
    editMode.value = props.application !== undefined;
    key.value = '';
  },
);

function onHide() {
  emit('update:modelValue', false);
}

function onCancel() {
  emit('cancel');
}

function onSave() {
  if (key.value) {
    selected.value.key = key.value;
  }
  applicationStore
    .save(selected.value)
    .then(() => {
      notifySuccess(t('application.saved'));
      emit('saved');
    })
    .catch(() => {
      notifyError(t('application.save_failed'));
    })
    .finally(() => {
      emit('update:modelValue', false);
    });
}

function generateKey() {
  key.value = applicationStore.generateKey();
}

function copyKeyToClipboard() {
  if (key.value) {
    copyToClipboard(key.value).then(() => {
      notifyInfo(t('application.key_copied'));
    });
  }
}
</script>
