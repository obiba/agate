<template>
  <div>
    <div class="text-bold">
      {{ t('user.attributes.title') }}
    </div>
    <div class="text-hint q-mb-sm">
      {{ t('user.attributes.hint') }}
    </div>
    <q-list>
      <q-item v-for="(entry, idx) in attributes" :key="idx" class="q-pa-none">
        <q-item-section>
          <div class="row q-col-gutter-md">
            <div class="col">
              <q-input
                v-model="entry.name"
                :label="t('name')"
                dense
                lazy-rules
                :rules="[(val) => !!val || t('required')]"
                @update:model-value="onUpdate"
              />
            </div>
            <div class="col">
              <q-input
                v-model="entry.value"
                :label="t('value')"
                dense
                lazy-rules
                :rules="[(val) => !!val || t('required')]"
                @update:model-value="onUpdate"
              />
            </div>
          </div>
        </q-item-section>
        <q-item-section side>
          <q-btn flat icon="delete" color="accent" size="sm" @click="onDelete(idx)" />
        </q-item-section>
      </q-item>
    </q-list>
    <q-btn color="primary" icon="add" size="sm" @click="attributes.push({ name: '', value: '' })" />
  </div>
</template>

<script setup lang="ts">
import type { AttributeDto } from 'src/models/Agate';

interface Props {
  modelValue: AttributeDto[] | undefined;
}

const props = defineProps<Props>();
const emit = defineEmits(['update:modelValue']);
const { t } = useI18n();

const attributes = ref<AttributeDto[]>(props.modelValue ? [...props.modelValue] : []);

function onDelete(index: number) {
    attributes.value.splice(index, 1);
    emit('update:modelValue', attributes.value.filter((attr) => attr.name && attr.value));
}

function onUpdate() {
  emit('update:modelValue', attributes.value.filter((attr) => attr.name && attr.value));
}
</script>
