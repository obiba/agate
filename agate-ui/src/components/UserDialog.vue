<template>
  <q-dialog v-model="showDialog" persistent @hide="onHide">
    <q-card class="dialog-md">
      <q-card-section>
        <div class="text-h6">{{ t(editMode ? 'user.edit' : 'user.add') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-form>
          <div class="row q-col-gutter-md q-mb-md">
            <div class="col">
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
            </div>
            <div class="col">
              <q-input
                v-model="selected.email"
                :label="t('email') + ' *'"
                :hint="t('email_hint')"
                dense
                lazy-rules
                :rules="[
                  (val) => !!val || t('email_required'),
                  (val) => validateEmailFormat(val) || t('email_invalid'),
                ]"
              />
            </div>
          </div>
          <div class="row q-col-gutter-md q-mb-md">
            <div class="col-5">
              <q-input v-model="selected.firstName" :label="t('user.firstName')" dense />
            </div>
            <div class="col-5">
              <q-input v-model="selected.lastName" :label="t('user.lastName')" dense />
            </div>
            <div class="col-2">
              <q-select
                v-model="selected.preferredLanguage"
                :label="t('user.language')"
                :options="languageOptions"
                dense
                emit-value
                map-options
              />
            </div>
          </div>
          <div v-if="!editMode" class="row q-col-gutter-md q-mb-md">
            <div class="col">
              <q-select
                v-model="selected.realm"
                :label="t('realm.realm')"
                :hint="t(`user.realm_hint`)"
                :options="realmOptions"
                dense
                emit-value
                map-options
                @update:model-value="onRealmChange"
              />
            </div>
            <div v-if="showPassword" class="col">
              <q-input
                v-model="password"
                :label="t('user.password') + ' *'"
                :type="passwordVisible ? 'text' : 'password'"
                dense
                lazy-rules
                :rules="[
                  (val) => !!val || t('user.password_required'),
                  (val) => (val?.trim().length ?? 0) >= 8 || t('user.password_min_length', { min: 8 }),
                ]"
              >
                <template v-slot:after>
                  <q-btn
                    round
                    dense
                    size="sm"
                    :title="t('user.show_password')"
                    flat
                    icon="visibility"
                    @click="passwordVisible = !passwordVisible"
                  />
                  <q-btn
                    round
                    dense
                    size="sm"
                    :title="t('user.copy_password')"
                    flat
                    icon="content_copy"
                    @click="copyPasswordToClipboard"
                  />
                  <q-btn
                    round
                    dense
                    size="sm"
                    :title="t('user.generate_password')"
                    flat
                    icon="lock_reset"
                    @click="generatePassword"
                  />
                </template>
              </q-input>
              <div class="text-hint">{{ t('user.password_hint') }}</div>
            </div>
          </div>
          <div class="row q-col-gutter-md q-mb-md">
            <div class="col">
              <q-select
                v-model="selected.role"
                :label="t('role')"
                :hint="t(`user.role_hint.${selected.role}`)"
                :options="roleOptions"
                dense
                emit-value
                map-options
              />
            </div>
            <div class="col">
              <q-select
                v-model="selected.status"
                :label="t('status')"
                :hint="t(`user.status_hint.${selected.status}`)"
                :options="statusOptions"
                dense
                emit-value
                map-options
              />
            </div>
          </div>
          <div class="row q-col-gutter-md q-mb-md">
            <div class="col">
              <q-select
                v-model="selected.groups"
                :label="t('groups')"
                :hint="t('user.groups_hint')"
                :options="groupOptions"
                dense
                emit-value
                map-options
                use-chips
                multiple
              />
            </div>
            <div class="col">
              <q-select
                v-model="selected.applications"
                :label="t('applications')"
                :hint="t('user.applications_hint')"
                :options="applicationOptions"
                dense
                emit-value
                map-options
                use-chips
                multiple
              />
            </div>
          </div>
        </q-form>

        <div class="text-bold q-mt-lg">
          {{ t('user.attributes.title') }}
        </div>
        <div class="text-hint q-mb-sm">
          {{ t('user.attributes.hint') }}
        </div>
        <schema-form ref="sfForm" v-model="sfModel" :schema="sfSchema" />
        <user-attributes-list v-model="selectedAttributes" />
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
import { copyToClipboard } from 'quasar';
import type { AttributeDto, UserDto } from 'src/models/Agate';
import { notifyError, notifyInfo, notifySuccess } from 'src/utils/notify';
import UserAttributesList from 'src/components/UserAttributesList.vue';
import { attributesToSchema, splitAttributes } from 'src/utils/attributes';
import SchemaForm from 'src/components/SchemaForm.vue';

interface DialogProps {
  modelValue: boolean;
  user: UserDto | undefined;
}

const { t } = useI18n();
const userStore = useUserStore();
const groupStore = useGroupStore();
const applicationStore = useApplicationStore();
const realmStore = useRealmStore();
const systemStore = useSystemStore();
const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'saved', 'cancel']);
const sfForm = ref();
const sfSchema = ref();
const sfModel = ref();
const showDialog = ref(props.modelValue);
const selected = ref<UserDto>(
  props.user ??
    ({
      realm: 'agate-user-realm',
      role: 'agate-user',
      status: 'INACTIVE',
      preferredLanguage: '',
    } as UserDto),
);
const editMode = ref(false);
const password = ref('');
const passwordVisible = ref(false);
const selectedAttributes = ref([] as AttributeDto[]);

const roleOptions = computed(() =>
  ['agate-user', 'agate-administrator'].map((role) => ({ label: t(`user.role.${role}`), value: role })),
);
const statusOptions = computed(() =>
  ['ACTIVE', 'APPROVED', 'INACTIVE', 'PENDING'].map((status) => ({ label: t(`user.status.${status}`), value: status })),
);
const groupOptions = computed(() => groupStore.groups?.map((group) => ({ label: group.name, value: group.id })) ?? []);
const applicationOptions = computed(
  () => applicationStore.applications?.map((app) => ({ label: app.name, value: app.id })) ?? [],
);
const realmOptions = computed(() => [
  { label: 'agate-user-realm', value: 'agate-user-realm' },
  ...(realmStore.realms?.map((realm) => ({ label: realm.name, value: realm.id })) ?? []),
]);
const languageOptions = computed(
  () => systemStore.configurationPublic.languages?.map((lang) => ({ label: lang, value: lang })) ?? [],
);
const showPassword = computed(() => !editMode.value && selected.value.realm === 'agate-user-realm');
const isValid = computed(
  () =>
    selected.value.name &&
    selected.value.name.trim().length >= 3 &&
    selected.value.email &&
    validateEmailFormat(selected.value.email) &&
    (!showPassword.value || (password.value && password.value.trim().length >= 8)) &&
    sfForm.value?.validate(),
);

onMounted(() => {
  groupStore.init();
  applicationStore.init();
  realmStore.init();
  systemStore.init();
  systemStore.initPub();
});

watch(
  () => props.modelValue,
  (value) => {
    showDialog.value = value;
    selected.value = props.user
      ? { ...props.user }
      : ({
          realm: 'agate-user-realm',
          role: 'agate-user',
          status: 'INACTIVE',
          preferredLanguage: languageOptions.value.length ? languageOptions.value[0]?.value : '',
        } as UserDto);
    editMode.value = props.user !== undefined;
    password.value = '';
    passwordVisible.value = false;

    if (value) {
      sfSchema.value = attributesToSchema(systemStore.userAttributes, '', '');
      const { custom, specific } = splitAttributes(selected.value.attributes || [], systemStore.userAttributes || []);
      sfModel.value = custom.map((attr) => ({ [attr.name]: attr.value })).reduce((a, b) => ({ ...a, ...b }), {});
      selectedAttributes.value = specific;
    }
  },
);

function onHide() {
  emit('update:modelValue', false);
}

function onCancel() {
  emit('cancel');
}

function onSave() {
  selected.value.attributes = [
    ...Object.entries(sfModel.value).map(([name, value]) => ({ name, value: value as string })),
    ...selectedAttributes.value,
  ];

  userStore
    .save(selected.value, password.value)
    .then(() => {
      notifySuccess('user.saved');
      emit('saved');
    })
    .catch(() => {
      notifyError('user.save_failed');
    })
    .finally(() => {
      emit('update:modelValue', false);
    });
}

function validateEmailFormat(val: string): boolean {
  if (val) {
    return val.match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/) !== null;
  }
  return true;
}

function onRealmChange() {
  password.value = '';
}

function generatePassword() {
  password.value = userStore.generatePassword();
}

function copyPasswordToClipboard() {
  if (password.value) {
    copyToClipboard(password.value).then(() => {
      notifyInfo(t('user.password_copied'));
    });
  }
}
</script>
