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
          <div class="q-mb-md">
            <div>{{ t('application.scopes') }}</div>
            <div class="text-hint">{{ t('application.scopes_hint') }}</div>
            <q-list>
              <q-item v-for="scope in selected.scopes" :key="scope.name" clickable>
                <q-item-section>
                  <div class="row q-col-gutter-md">
                    <q-input v-model="scope.name" :label="t('name') + ' *'" :hint="t('name_hint')" dense />
                    <q-input v-model="scope.description" :label="t('description')" dense />
                  </div>
                </q-item-section>
                <q-item-section side>
                  <q-btn flat dense icon="delete" size="sm" color="negative" @click="removeScope(scope.name)" />
                </q-item-section>
              </q-item>
            </q-list>
            <q-btn
              icon="add"
              no-caps
              color="primary"
              size="sm"
              :label="t('application.add_scope')"
              @click="onAddScope"
              class="q-mt-md"
            />
          </div>
          <div class="q-mb-md">
            <div>{{ t('application.realms_groups') }}</div>
            <div class="text-hint">{{ t('application.realms_groups_hint') }}</div>
            <q-list>
              <q-item v-for="realmGroup in selected.realmGroups" :key="realmGroup.realm" clickable>
                <q-item-section>
                  <div class="row q-col-gutter-md">
                    <q-select
                      v-model="realmGroup.realm"
                      :label="t('realm.realm')"
                      :options="realmOptions"
                      emit-value
                      map-options
                      dense
                    />
                    <q-select
                      v-model="realmGroup.groups"
                      :label="t('groups')"
                      :options="groupOptions"
                      multiple
                      emit-value
                      map-options
                      use-chips
                      dense
                      style="min-width: 200px"
                    />
                  </div>
                </q-item-section>
                <q-item-section side>
                  <q-btn
                    flat
                    dense
                    icon="delete"
                    size="sm"
                    color="negative"
                    @click="removeRealmGroups(realmGroup.realm)"
                  />
                </q-item-section>
              </q-item>
            </q-list>
            <q-btn
              icon="add"
              no-caps
              color="primary"
              size="sm"
              :label="t('application.add_realm_groups')"
              @click="onAddRealmGroups"
              class="q-mt-md"
            />
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
const groupStore = useGroupStore();
const realmStore = useRealmStore();

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

const groupOptions = computed(() => groupStore.groups?.map((group) => ({ label: group.name, value: group.id })) ?? []);
const realmOptions = computed(
  () => realmStore.realms?.map((realm) => ({ label: realm.name, value: realm.id || '' })) ?? [],
);
const isValid = computed(
  () =>
    selected.value.name &&
    selected.value.name.trim().length >= 3 &&
    (editMode || (key.value && key.value.trim().length >= 8)),
);

onMounted(() => {
  applicationStore.init();
  groupStore.init();
  realmStore.init();
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

function removeScope(name: string) {
  selected.value.scopes = selected.value.scopes.filter((s) => s.name !== name);
}

function onAddScope() {
  if (!selected.value.scopes) {
    selected.value.scopes = [];
  }
  selected.value.scopes.push({ name: 'scope-' + (selected.value.scopes.length + 1) });
}

function removeRealmGroups(realm: string) {
  selected.value.realmGroups = selected.value.realmGroups.filter((rg) => rg.realm !== realm);
}

function onAddRealmGroups() {
  if (!realmOptions.value.length) {
    return;
  }
  if (!selected.value.realmGroups) {
    selected.value.realmGroups = [];
  }
  const realm =
    realmOptions.value.find((rlm) => !selected.value.realmGroups.map((rlmGrps) => rlmGrps.realm).includes(rlm.value))
      ?.value || '';
  if (!realm) {
    return;
  }
  selected.value.realmGroups.push({ realm, groups: [] });
}
</script>
