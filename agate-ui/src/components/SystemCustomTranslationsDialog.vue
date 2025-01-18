<template>
  <q-dialog v-model="showDialog" persistent @hide="onHide">
    <q-card class="dialog-md">
      <q-card-section>
        <div class="text-h6">{{ t('system.translations.add') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <div class="text-help q-mb-md">{{ t('system.translations.add_hint', {language: language.toUpperCase()}) }}</div>
        <q-form ref="formRef">
          <q-input
            v-model="newAttribue.name"
            dense
            type="text"
            :label="t('name') + ' *'"
            class="q-mb-md"
            lazy-rules
            :rules="[validateRequired, validateUnique]"
          >
          </q-input>
          <q-input v-model="newAttribue.value" :label="t('value')" class="q-mb-md" dense type="text" lazy-rules />
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" @click="onCancel" v-close-popup />
        <q-btn flat :label="t('add')" color="primary" @click="onSave" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { AttributeDto } from 'src/models/Agate';

const { t } = useI18n();

interface DialogProps {
  modelValue: boolean;
  translationKeys: string[];
  language: string;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'added', 'cancel']);
const formRef = ref();
const showDialog = ref(props.modelValue);
const newAttribue = ref<AttributeDto>({} as AttributeDto);

function validateRequired(value: string) {
  return !!value || t('name_required');
}

function validateUnique(value: string) {
  if (value) {
    const exists = props.translationKeys.find((key) => key === value);
    return !exists || t('system.translations.name_exists');
  }
  return true;
}

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      newAttribue.value = {} as AttributeDto;
    }

    showDialog.value = value;
  },
);

function onHide() {
  emit('update:modelValue', false);
}

function onCancel() {
  emit('cancel');
}

async function onSave() {
  const valid = await formRef.value.validate();
  if (valid) {
    if (!newAttribue.value.value) {
      newAttribue.value.value = newAttribue.value.name;
    }

    emit('added', newAttribue.value);
    emit('update:modelValue', false);
  }
}
</script>
