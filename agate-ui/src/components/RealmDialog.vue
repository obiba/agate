<template>
  <q-dialog v-model="showDialog" persistent @hide="onHide">
    <q-card class="dialog-md">
      <q-card-section>
        <div class="text-h6">{{ t(editMode ? 'realm.edit' : 'realm.add') }}</div>
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
            class="q-mb-md"
          />
          <localized-input
            v-model="selected.title"
            :label="t('realm.title')"
            :hint="t('realm.title_hint')"
            required
            class="q-mb-md"
          />
          <div class="row q-col-gutter-md q-mb-md">
            <div class="col-12 col-sm-3">
              <q-toggle
                :label="t(`realm.status.${selected.status}`)"
                color="positive"
                false-value="INACTIVE"
                true-value="ACTIVE"
                v-model="selected.status"
              />
            </div>
            <div class="col-12 col-sm-9">
              <q-select
                v-model="selected.type"
                :label="t('type') + ' *'"
                :options="typeOptions"
                dense
                emit-value
                map-options
                lazy-rules
                @update:model-value="onTypeChange"
              />
            </div>
          </div>
          <div class="row q-col-gutter-md q-mb-md">
            <div class="col-12 col-sm-3">
              <q-checkbox
                v-model="selected.forSignup"
                :label="t('realm.for_signup')"
                @update:model-value="selected.groups = []"
              />
            </div>
            <div class="col-12 col-sm-9">
              <q-select
                v-model="selected.groups"
                :label="t('groups')"
                :hint="t('realm.groups_hint')"
                :disable="!selected.forSignup"
                :options="groupOptions"
                dense
                emit-value
                map-options
                multiple
                use-chips
                lazy-rules
              />
            </div>
          </div>
          <q-input
            v-model="selected.publicUrl"
            :label="t('realm.public_url')"
            :hint="t('realm.public_url_hint')"
            dense
            class="q-mb-md"
          />
          <q-input
            v-model="selected.domain"
            :label="t('realm.sso_domain')"
            :hint="t('realm.sso_domain_hint')"
            dense
            class="q-mb-md"
          />

          <q-card bordered flat>
            <q-card-section>
              <oidc-form v-if="selected.type === 'agate-oidc-realm'" v-model="selected.content" />
              <ldap-form v-if="selected.type === 'agate-ldap-realm'" v-model="selected.content" />
              <ad-form v-if="selected.type === 'agate-ad-realm'" v-model="selected.content" />
              <jdbc-form v-if="selected.type === 'agate-jdbc-realm'" v-model="selected.content" />
              <user-info-mappings-form
                v-if="selected.type === 'agate-oidc-realm'"
                v-model="selected.userInfoMappings"
              />
            </q-card-section>
          </q-card>
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
import LocalizedInput from 'src/components/LocalizedInput.vue';
import OidcForm from 'src/components/realms/OidcForm.vue';
import LdapForm from 'src/components/realms/LdapForm.vue';
import AdForm from 'src/components/realms/AdForm.vue';
import JdbcForm from 'src/components/realms/JdbcForm.vue';
import UserInfoMappingsForm from 'src/components/realms/UserInfoMappingsForm.vue';
import type { RealmConfigDto, RealmConfigSummaryDto } from 'src/models/Agate';
import { notifyError, notifySuccess } from 'src/utils/notify';

const { t } = useI18n();
const groupStore = useGroupStore();
const realmStore = useRealmStore();

interface DialogProps {
  modelValue: boolean;
  realmSummary: RealmConfigSummaryDto | undefined;
  duplicate: boolean;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'saved', 'cancel']);

const showDialog = ref(props.modelValue);
const selected = ref<RealmConfigDto>({} as RealmConfigDto);
const editMode = ref(false);

const typeOptions = computed(() =>
  ['agate-oidc-realm', 'agate-ad-realm', 'agate-ldap-realm', 'agate-jdbc-realm']
    .map((type) => ({
      label: t(`realm.type.${type}`),
      value: type,
    }))
    .sort((a, b) => a.label.localeCompare(b.label)),
);
const groupOptions = computed(() =>
  (groupStore.groups?.map((group) => ({ label: group.name, value: group.id })) ?? []).sort((a, b) =>
    a.label.localeCompare(b.label),
  ),
);
const isValid = computed(() => selected.value.name && selected.value.name.trim().length >= 3);

const OIDCUserInfoDefaultMappings = [
  { key: 'username', value: 'preferred_username' },
  { key: 'email', value: 'email' },
  { key: 'firstname', value: 'given_name' },
  { key: 'lastname', value: 'family_name' },
];

onMounted(() => {
  groupStore.init();
});

watch(
  () => props.modelValue,
  (value) => {
    showDialog.value = value;
    if (props.realmSummary) {
      realmStore.getConfig(props.realmSummary.name).then((config) => {
        selected.value = config;
        if (props.duplicate) {
          config.name = '';
          delete config.id;
        }
      });
    } else {
      selected.value = {
        type: typeOptions.value.find((rlm) => rlm.value === 'agate-oidc-realm')?.value,
        userInfoMappings: OIDCUserInfoDefaultMappings,
        status: 'INACTIVE',
        forSignup: false,
      } as RealmConfigDto;
    }
    editMode.value = props.realmSummary !== undefined && !props.duplicate;
  },
);

function onHide() {
  emit('update:modelValue', false);
}

function onCancel() {
  emit('cancel');
}

function onTypeChange() {
  selected.value.content = '';
  if (selected.value.type === 'agate-oidc-realm') {
    selected.value.userInfoMappings = OIDCUserInfoDefaultMappings;
  }
}

function onSave() {
  realmStore
    .save(selected.value)
    .then(() => {
      notifySuccess(t('realm.saved'));
      emit('saved');
      emit('update:modelValue', false);
    })
    .catch((err) => {
      notifyError(err);
    });
}
</script>
