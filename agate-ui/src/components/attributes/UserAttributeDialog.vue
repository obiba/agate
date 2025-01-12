<template>
  <q-dialog v-model="showDialog" persistent @hide="onHide">
    <q-card class="dialog-md">
      <q-card-section>
        <div class="text-h6">{{ editMode ? t('user.attributes.update') : t('user.attributes.add') }}</div>
      </q-card-section>

      <q-separator />
      <q-card-section>
        <q-form ref="formRef">
          <q-input
            v-model="newAttribue.name"
            dense
            type="text"
            :label="t('name') + ' *'"
            class="q-mb-md"
            lazy-rules
            :rules="[validateRequired, validateUnique]"
            :disable="editMode"
          >
          </q-input>
          <q-input
            v-model="newAttribue.value"
            dense
            type="text"
            :label="t('value')"
            class="q-mb-md"
            lazy-rules
          />
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" @click="onCancel" v-close-popup />
        <q-btn flat :label="t('save')" color="primary" @click="onSave" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script lang="ts">
export default defineComponent({
  name: 'UserAttributeDialog',
});
</script>

<script setup lang="ts">
import type { AttributeDto } from 'src/models/Agate';

interface DialogProps {
  modelValue: boolean;
  attributes: AttributeDto[];
  attribute: AttributeDto | undefined;
}

const { t } = useI18n();
const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'saved', 'cancel']);
const formRef = ref();
const showDialog = ref(props.modelValue);
const newAttribue = ref<AttributeDto>({} as AttributeDto);
const editMode = computed(() => !!props.attribute && !!props.attribute.name);
const attributes = computed<AttributeDto[]>(() => props.attributes || [] as AttributeDto[]);

function validateRequired(value: string) {
  return !!value || t('name_required');
}

function validateUnique(value: string) {
  if (attributes) {
    const exists = attributes.value.find((attr) => attr.name === value);
    return !exists || t('user.attributes.name_exists');
  }
  return true;
}

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      if (props.attribute) {
        newAttribue.value = { ...props.attribute } as AttributeDto;
      } else {
        newAttribue.value = {} as AttributeDto;
      }
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
      emit('saved', newAttribue.value);
      emit('update:modelValue', false);
  }
}
</script>
