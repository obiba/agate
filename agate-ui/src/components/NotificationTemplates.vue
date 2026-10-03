<template>
  <div>
    <q-table
      :rows="templates"
      flat
      dense
      row-key="name"
      :columns="columns"
      :pagination="initialPagination"
      :filter="filter"
    >
      <template v-slot:top-left>
        <q-btn size="sm" icon="add" color="primary" :label="t('add')" @click="onAdd" />
      </template>
      <template v-slot:top-right>
        <q-input v-model="filter" debounce="300" :placeholder="t('search')" dense clearable>
          <template v-slot:prepend>
            <q-icon name="search" />
          </template>
        </q-input>
      </template>
      <template v-slot:body="props">
        <q-tr
          :props="props"
          @mouseover="toolsVisible[props.row.name] = true"
          @mouseleave="toolsVisible[props.row.name] = false"
        >
          <q-td key="name" :props="props">
            <a href="javascript:void(0)" @click="onEdit(props.row)">{{ props.row.name }}</a>
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
                @click="onEdit(props.row)"
              />
              <q-btn
                v-if="props.row.custom"
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :icon="toolsVisible[props.row.name] ? (props.row.bundled ? 'restore' : 'delete') : 'none'"
                :title="props.row.bundled ? t('notification_templates.revert') : t('delete')"
                class="q-ml-xs"
                @click="onShowDelete(props.row)"
              />
            </div>
          </q-td>
          <q-td key="state" :props="props">
            <q-badge
              :color="stateColor(props.row)"
              :label="t(`notification_templates.states.${stateOf(props.row)}`, { folder: props.row.inheritedFrom })"
            />
          </q-td>
        </q-tr>
      </template>
    </q-table>

    <q-dialog v-model="showEdit" persistent>
      <q-card class="dialog-lg">
        <q-card-section>
          <div class="text-h6">{{ isNew ? t('notification_templates.add') : editName }}</div>
        </q-card-section>
        <q-separator />
        <q-card-section>
          <q-input
            v-if="isNew"
            v-model="editName"
            dense
            :label="t('name')"
            :hint="t('notification_templates.name_hint')"
            :rules="[(val) => NAME_PATTERN.test(val) || t('notification_templates.name_invalid')]"
            class="q-mb-md"
          />
          <q-tabs v-model="tab" dense align="left" active-color="primary" indicator-color="primary" no-caps>
            <q-tab name="edit" :label="t('edit')" />
            <q-tab name="preview" :label="t('notification_templates.preview')" />
          </q-tabs>
          <q-separator />
          <q-tab-panels v-model="tab">
            <q-tab-panel name="edit" class="q-px-none">
              <v-ace-editor
                v-model:value="editContent"
                lang="ftl"
                theme="monokai"
                :options="{ useWorker: false, tabSize: 2, wrap: true, showPrintMargin: false }"
                style="height: 500px; border: 1px solid #ddd"
              />
              <div class="text-help q-mt-sm">{{ t('notification_templates.content_hint') }}</div>
            </q-tab-panel>
            <q-tab-panel name="preview" class="q-px-none">
              <q-btn-toggle
                v-if="languages.length > 1"
                v-model="previewLocale"
                :options="languages.map((lang) => ({ label: lang.toUpperCase(), value: lang }))"
                dense
                no-caps
                unelevated
                toggle-color="primary"
                class="q-mb-sm"
              />
              <q-banner v-if="previewError" dense class="bg-red-1 text-negative q-mb-xs" style="white-space: pre-wrap">
                {{ previewError }}
              </q-banner>
              <!-- sandbox: rendered HTML cannot run scripts nor access the admin app -->
              <iframe
                :srcdoc="previewHtml"
                sandbox=""
                style="height: 500px; width: 100%; border: 1px solid #ddd; background: white"
              />
              <div class="text-help q-mt-xs">{{ t('notification_templates.preview_hint') }}</div>
            </q-tab-panel>
          </q-tab-panels>
        </q-card-section>
        <q-separator />
        <q-card-actions align="right" class="bg-grey-3">
          <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
          <q-btn flat :label="t('save')" color="primary" :disable="!NAME_PATTERN.test(editName)" @click="onTrySave" />
        </q-card-actions>
      </q-card>
    </q-dialog>

    <confirm-dialog
      v-model="showDelete"
      :title="selected?.bundled ? t('notification_templates.revert') : t('delete')"
      :text="
        t(selected?.bundled ? 'notification_templates.revert_confirm' : 'notification_templates.delete_confirm', {
          name: selected?.name,
        })
      "
      @confirm="onDelete"
    />

    <confirm-dialog
      v-model="showOverwrite"
      :title="t('save')"
      :text="t('notification_templates.overwrite_confirm', { name: editName })"
      @confirm="onSave"
    />
  </div>
</template>

<script setup lang="ts">
import type { NotificationTemplate } from 'src/stores/system';
import { DefaultAlignment } from 'src/components/models';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import { notifyError, notifySuccess } from 'src/utils/notify';
import { VAceEditor } from 'vue3-ace-editor';

interface Props {
  // application folder, none for Agate's own templates
  folder?: string | undefined;
  // fallback folder of the application (resolved server side), refreshes the inherited templates when changed
  fallback?: string | undefined;
}

const props = defineProps<Props>();

const { t } = useI18n();
const systemStore = useSystemStore();

const NAME_PATTERN = /^[A-Za-z0-9_-]+$/;

const initialPagination = ref({ descending: false, page: 1, rowsPerPage: 10 });
const templates = ref<NotificationTemplate[]>([]);
const filter = ref('');
const toolsVisible = ref<Record<string, boolean>>({});
const showEdit = ref(false);
const showDelete = ref(false);
const showOverwrite = ref(false);
const isNew = ref(false);
const editName = ref('');
const editContent = ref('');
const selected = ref<NotificationTemplate>();
const tab = ref('edit');
const languages = computed<string[]>(() => systemStore.configuration.languages || []);
const previewLocale = ref('');
const previewHtml = ref('');
const previewError = ref('');

const columns = computed(() => [
  { name: 'name', label: t('name'), field: 'name', align: DefaultAlignment, sortable: true },
  { name: 'state', label: t('notification_templates.state'), field: 'name', align: DefaultAlignment },
]);

function stateOf(tpl: NotificationTemplate) {
  if (tpl.inheritedFrom) return 'inherited';
  if (tpl.bundled && tpl.custom) return 'overridden';
  return tpl.custom ? 'custom' : 'default';
}

function stateColor(tpl: NotificationTemplate) {
  return { overridden: 'warning', custom: 'primary', default: 'grey', inherited: 'blue-grey-4' }[stateOf(tpl)];
}

function refresh() {
  systemStore
    .getNotificationTemplates(props.folder)
    .then((data) => (templates.value = data))
    .catch(notifyError);
}

function onAdd() {
  isNew.value = true;
  editName.value = '';
  editContent.value = '';
  tab.value = 'edit';
  previewLocale.value = defaultPreviewLocale();
  showEdit.value = true;
}

function onEdit(tpl: NotificationTemplate) {
  systemStore
    .getNotificationTemplate(tpl.name, props.folder)
    .then((content) => {
      isNew.value = false;
      editName.value = tpl.name;
      editContent.value = content;
      tab.value = 'edit';
      previewLocale.value = defaultPreviewLocale();
      showEdit.value = true;
    })
    .catch(notifyError);
}

// adding a template with the name of an existing custom one would overwrite it
function onTrySave() {
  if (isNew.value && templates.value.some((tpl) => tpl.name === editName.value && tpl.custom)) showOverwrite.value = true;
  else onSave();
}

function onSave() {
  systemStore
    .saveNotificationTemplate(editName.value, editContent.value, props.folder)
    .then(() => {
      showEdit.value = false;
      notifySuccess(t('notification_templates.saved'));
      refresh();
    })
    .catch(notifyError);
}

function preview() {
  systemStore
    .previewNotificationTemplate(
      NAME_PATTERN.test(editName.value) ? editName.value : 'preview',
      editContent.value,
      props.folder,
      previewLocale.value,
    )
    .then((html) => {
      previewHtml.value = html;
      previewError.value = '';
    })
    .catch((err) => {
      previewHtml.value = '';
      // error response is read as text
      try {
        previewError.value = JSON.parse(err.response?.data).message || err.message;
      } catch {
        previewError.value = err.message;
      }
    });
}

watch([tab, previewLocale], () => {
  if (tab.value === 'preview') preview();
});

// language of the template name suffix (e.g. confirmationEmail_fr), else the default one
function defaultPreviewLocale() {
  const suffix = editName.value.match(/_([a-z]{2})(_[A-Z]{2})?$/)?.[1];
  return suffix && languages.value.includes(suffix) ? suffix : systemStore.defaultLanguage;
}

function onShowDelete(tpl: NotificationTemplate) {
  selected.value = tpl;
  showDelete.value = true;
}

function onDelete() {
  if (!selected.value) return;
  systemStore.deleteNotificationTemplate(selected.value.name, props.folder).then(refresh).catch(notifyError);
}

watch([() => props.folder, () => props.fallback], refresh, { immediate: true });

onMounted(() => {
  // languages, not loaded by the application page
  if (!systemStore.configuration.languages) systemStore.init();
});
</script>
