<template>
  <div>
    <div class="q-px-md">
      <q-tabs
        v-model="selectedLanguage"
        dense
        class="text-grey"
        active-color="primary"
        indicator-color="primary"
        align="justify"
      >
        <q-tab v-for="language in languages" :key="language" :name="language" :label="language" />
      </q-tabs>

      <q-separator />
    </div>

    <div class="q-px-md">
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
            <q-btn size="sm" icon="add" color="primary" :label="t('add')" @click="onAdd" />
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
            <q-input type="text" v-model="props.row.value" dense @update:model-value="onValueChanged(props.row)" />
          </q-td>
        </template>
      </q-table>
      <div v-if="dirty" class="box-warning q-mt-md row items-center justify-center">
        <div class="col">{{ t('system.translations.apply_changes') }}</div>
        <div class="col-auto">
          <q-btn size="sm" icon="check" color="secondary" :label="t('apply')" :disable="!dirty" @click="onApply" />
        </div>
      </div>
    </div>

    <confirm-dialog
      v-model="showDelete"
      :title="t('user.remove')"
      :text="t('system.translations.remove_confirm', { count: selectedTranslations.length })"
      @confirm="doDelete"
    />

    <system-custom-translations-dialog
      v-model="showAdd"
      :translation-keys="translationKeys"
      :language="languages[0] || systemStore.defaultLanguage"
      @added="onAdded"
      @cancel="showAdd = false"
    />
  </div>
</template>

<script setup lang="ts">
import type { AttributeDto } from 'src/models/Agate';
import { DefaultAlignment } from 'src/components/models';
import { translationAsMap, mapAsTranslation } from 'src/utils/translations';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import SystemCustomTranslationsDialog from 'src/components/SystemCustomTranslationsDialog.vue';

const systemStore = useSystemStore();
const { t } = useI18n();

const initialPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 10,
});

const filter = ref('');
const dirty = ref(false);
const showAdd = ref(false);
const showDelete = ref(false);
const allTranslations = ref<Record<string, AttributeDto[]>>({});
const selectedLanguage = ref();
const translations = computed(
  () =>
    allTranslations.value[selectedLanguage.value]?.filter((app) =>
      filter.value ? app.name.toLowerCase().includes(filter.value.toLowerCase()) : true,
    ) || [],
);
const translationKeys = computed(() => (allTranslations.value[systemStore.defaultLanguage] || []).map((x) => x.name));
const selectedTranslations = ref<AttributeDto[]>([]);
const languages = computed<string[]>(() => systemStore.configuration.languages || []);
const columns = computed(() => [
  { name: 'name', label: t('name'), field: 'name', align: DefaultAlignment },
  { name: 'value', label: t('value'), field: 'value', align: DefaultAlignment },
]);

function onValueChanged(row: AttributeDto) {
  dirty.value = true;
  if (!row.value || row.value.length === 0) {
    row.value = row.name;
  }
}

function onAdd() {
  showAdd.value = true;
}

function onAdded(newTranslation: AttributeDto) {
  selectedTranslations.value.splice(0);
  dirty.value = true;
  showAdd.value = false;

  languages.value.forEach((lang) => {
    if (!allTranslations.value[lang]) {
      allTranslations.value[lang] = [];
    }

    allTranslations.value[lang].push({
      name: newTranslation.name,
      value: newTranslation.value || newTranslation.name,
    });
  });
}

function onApply() {
  dirty.value = false;
  selectedTranslations.value.splice(0);
  systemStore.updateTranslation(mapAsTranslation(allTranslations.value)).then(() => {
    systemStore.init();
  });
}

function onDelete() {
  showDelete.value = true;
}

function doDelete() {
  dirty.value = true;
  selectedTranslations.value.forEach((translation) => {
    languages.value.forEach((lang) => {
      if (allTranslations.value[lang]) {
        allTranslations.value[lang] = allTranslations.value[lang].filter((x) => x.name !== translation.name);
      }
    });
  });

  selectedTranslations.value.splice(0);
  showDelete.value = false;
}

watch(
  () => systemStore.configuration.translations,
  (newValue) => {
    if (newValue) {
      selectedLanguage.value = systemStore.defaultLanguage;
      allTranslations.value = translationAsMap(newValue);
    }
  },
  { immediate: true },
);
</script>
