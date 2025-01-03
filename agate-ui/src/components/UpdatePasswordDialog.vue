<template>
  <q-dialog v-model="showDialog" persistent @hide="onHide">
    <q-card class="dialog-md">
      <q-card-section>
        <div class="text-h6">{{ t('user.update_password') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-form>
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
import type { UserDto } from 'src/models/Agate';
import { notifyError, notifyInfo, notifySuccess } from 'src/utils/notify';
import { copyToClipboard } from 'quasar';

const { t } = useI18n();
const userStore = useUserStore();

interface DialogProps {
  modelValue: boolean;
  user: UserDto | undefined;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'saved', 'cancel']);

const showDialog = ref(props.modelValue);
const password = ref('');
const passwordVisible = ref(false);
const isValid = computed(() => password.value && password.value.trim().length >= 8);

watch(
  () => props.modelValue,
  (value) => {
    showDialog.value = value;
    password.value = '';
    passwordVisible.value = false;
  },
);

function onHide() {
  emit('update:modelValue', false);
}

function onCancel() {
  emit('cancel');
}

function onSave() {
  if (!props.user) {
    return;
  }
  userStore
    .updatePassword(props.user, password.value)
    .then(() => {
      notifySuccess(t('user.password_updated'));
      emit('saved');
    })
    .catch(() => {
      notifyError(t('user.password_update_failed'));
    })
    .finally(() => {
      emit('update:modelValue', false);
    });
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
