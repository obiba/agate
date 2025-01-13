<template>
  <div>
    <span class="text-h6">
      {{ t('user.attributes.title') }}
    </span>
    <q-table
      :rows="filteredAttributes"
      flat
      row-key="name"
      :columns="columns"
      :pagination="initialPagination"
      :hide-pagination="filteredAttributes.length <= initialPagination.rowsPerPage"
    >
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
            </div>
          </q-td>
          <q-td key="value" :props="props">
            <span>{{ props.row.value }}</span>
          </q-td>
        </q-tr>
      </template>
    </q-table>

    <confirm-dialog
      v-model="showDelete"
      :title="t('user.attributes.remove')"
      :text="t('user.attributes.remove_confirm', { name: selected?.name })"
      @confirm="onDelete"
    />

    <user-attribute-dialog
      v-model="showEdit"
      :attributes="userAttributes"
      :attribute="selected"
      @saved="onSavedAttribute"
      @cancel="onCancel"
    />
  </div>
</template>

<script setup lang="ts">
import type { AttributeDto } from 'src/models/Agate';
import { DefaultAlignment } from 'src/components/models';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import UserAttributeDialog from 'src/components/attributes/UserAttributeDialog.vue';

interface Props {
  modelValue: AttributeDto[] | undefined;
}

const props = defineProps<Props>();
const emit = defineEmits(['update:modelValue']);
const { t } = useI18n();

const userAttributes = computed({
  get: () => props.modelValue ?? ([] as AttributeDto[]),
  set: (value: AttributeDto[]) => {
    emit('update:modelValue', value);
  },
});

const toolsVisible = ref<{ [key: string]: boolean }>({});
const initialPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 5,
});

const filteredAttributes = computed(
  () =>
    userAttributes.value.filter((attr) =>
      filter.value ? attr.name.toLowerCase().includes(filter.value.toLowerCase()) : true,
    ) || [],
);

const filter = ref('');
const selected = ref();
const showEdit = ref(false);
const showDelete = ref(false);

const columns = computed(() => [
  { name: 'name', label: t('name'), field: 'name', align: DefaultAlignment },
  { name: 'value', label: t('value'), field: 'value', align: DefaultAlignment },
]);

function onOverRow(row: AttributeDto) {
  toolsVisible.value[row.name] = true;
}

function onLeaveRow(row: AttributeDto) {
  toolsVisible.value[row.name] = false;
}

function onAdd() {
  selected.value = undefined;
  showEdit.value = true;
}

function onShowEdit(row: AttributeDto) {
  selected.value = row;
  showEdit.value = true;
}

function onShowDelete(row: AttributeDto) {
  selected.value = row;
  showDelete.value = true;
}

function onDelete() {
  if (selected.value) {
    const index = userAttributes.value.indexOf(selected.value);
    if (index !== -1) {
      userAttributes.value.splice(index, 1);
      emit('update:modelValue', userAttributes.value)
    }
  }
}

function onCancel() {
  showEdit.value = false;
}

function onSavedAttribute(newAttribute: AttributeDto) {
  if (newAttribute) {
    const index = userAttributes.value.findIndex((attr) => attr.name === newAttribute.name);
    if (index !== -1) {
      userAttributes.value[index] = newAttribute;
    } else {
      userAttributes.value.push(newAttribute);
    }
    emit('update:modelValue', userAttributes.value);
  }

  selected.value = undefined;
  showEdit.value = false;
}
</script>
