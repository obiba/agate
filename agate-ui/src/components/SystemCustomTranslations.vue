<template>
  <div>
    <div class="q-px-md">
      <q-tabs v-model="tab" dense class="text-grey" active-color="primary" indicator-color="primary" align="justify">
        <q-tab v-for="language in languages" :key="language" :name="language" :label="language" />
      </q-tabs>

      <q-separator />
    </div>

    <div class="q-px-md">
      <pre>{{ selectedTranslations.length }}</pre>
      <q-table
        :rows="translations"
        flat
        row-key="name"
        :columns="columns"
        :pagination="initialPagination"
        :hide-pagination="translations.length <= initialPagination.rowsPerPage"
        selection="multiple"
        v-model:selected="selectedTranslations"
      >
        <template v-slot:top-left>
          <div class="q-gutter-md">
            <q-btn size="sm" icon="check" color="primary" :label="t('save')" @click="onUpdate" />
            <q-btn
              size="sm"
              icon="delete"
              color="negative"
              :label="t('delete')"
              :disable="selectedTranslations.length < 1"
              @click="onDelete"
            />
          </div>
        </template>
        <template v-slot:top-right>
          <q-input v-model="filter" debounce="300" :placeholder="t('search')" dense clearable class="q-mr-md">
            <template v-slot:prepend>
              <q-icon name="search" />
            </template>
          </q-input>
        </template>
        <template v-slot:body-cell-name="props">
          <q-td :props="props">
            <span>{{ props.value }}</span>
          </q-td>
        </template>
        <template v-slot:body-cell-value="props">
          <q-td :props="props">
            <q-input type=text v-model="props.row.value" dense clearable  @clear="onClearValue(props.row)"/>
          </q-td>
        </template>
      </q-table>
    </div>

    <confirm-dialog
      v-model="showDelete"
      :title="t('user.remove')"
      :text="t('system.translations.remove', { count: selectedTranslations.length })"
      @confirm="doDelete"
    />
  </div>
</template>

<script setup lang="ts">
import type { AttributeDto } from 'src/models/Agate';
import { DefaultAlignment } from 'src/components/models';
import { translationAsMap, mapAsTranslation } from 'src/utils/translations';

const systemStore = useSystemStore();
const { t } = useI18n();

const initialPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 20,
});

const filter = ref('');
const showDelete = ref(false);
const tab = ref('en');
const allTranslations = ref<Record<string, AttributeDto[]>>({});
const translations = computed(
  () =>
    allTranslations.value[tab.value]?.filter((app) =>
      filter.value ? app.name.toLowerCase().includes(filter.value.toLowerCase()) : true,
    ) || [],
);
const selectedTranslations = ref<AttributeDto[]>([]);
const languages = computed(() => systemStore.configuration.languages);
const columns = computed(() => [
  { name: 'name', label: t('name'), field: 'name', align: DefaultAlignment },
  { name: 'value', label: t('value'), field: 'value', align: DefaultAlignment },
]);

function onClearValue(row: AttributeDto) {
  row.value = row.name;
}

function onUpdate() {
  systemStore.updateTranslation(mapAsTranslation(allTranslations.value));
}

function onDelete() {
  showDelete.value = true;
}

function doDelete() {

}

watch(
  () => systemStore.configuration.translations,
  (newValue) => {
    if (newValue) {
      allTranslations.value = translationAsMap(newValue);
    }
  },
  { immediate: true },
);
</script>
