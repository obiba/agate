<template>
  <q-dialog v-model="showDialog" persistent @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t('group.add_members') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <div class="text-hint q-mb-sm">{{ t('group.add_members_hint') }}</div>
        <q-form>
          <q-input
            v-model="filter"
            :label="t('search')"
            dense
            debounce="500"
            class="q-mb-md"
            @update:model-value="onSearch"
          >
            <template v-slot:append>
              <q-spinner v-if="loading" size="xs" />
            </template>
            <q-menu v-model="showMenu" no-focus no-refocus>
              <q-list bordered separator>
                <q-item v-for="(user, idx) in results" :key="idx" clickable @click="onSelectUser(user)">
                  <q-item-section>
                    <q-item-label>
                      <div class="text-bold">{{ user.name }}</div>
                      <div class="text-caption">{{ user.firstName }} {{ user.lastName }}</div>
                      <div class="text-hint">{{ user.email }}</div>
                    </q-item-label>
                  </q-item-section>
                </q-item>
              </q-list>
            </q-menu>
          </q-input>
          <q-list v-if="selected.length" bordered separator>
            <q-item v-for="(user, idx) in selected" :key="idx">
              <q-item-section>
                <q-item-label>
                  <div class="text-bold">{{ user.name }}</div>
                  <div class="text-caption">{{ user.firstName }} {{ user.lastName }}</div>
                  <div class="text-hint">{{ user.email }}</div>
                </q-item-label>
              </q-item-section>
              <q-item-section side>
                <q-btn flat icon="close" size="sm" dense @click.stop="onSelectUser(user)" />
              </q-item-section>
            </q-item>
          </q-list>
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" @click="onCancel" v-close-popup />
        <q-btn flat :label="t('add')" color="primary" @click="onSave" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { GroupDto, UserDto } from 'src/models/Agate';
import { notifyError, notifySuccess } from 'src/utils/notify';

const { t } = useI18n();
const userStore = useUserStore();

interface DialogProps {
  modelValue: boolean;
  group: GroupDto;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'saved', 'cancel']);

const showDialog = ref(props.modelValue);
const showMenu = ref(false);
const loading = ref(false);
const selected = ref<UserDto[]>([]);
const filter = ref('');
const results = ref<UserDto[]>([]);

watch(
  () => props.modelValue,
  (value) => {
    showDialog.value = value;
    results.value = [];
    selected.value = [];
    filter.value = '';
  },
);

function onHide() {
  emit('update:modelValue', false);
}

function onCancel() {
  emit('cancel');
}

function onSave() {
  if (!props.group.id || selected.value.length === 0) {
    return;
  }
  const promises = selected.value.map((user) => {
    if (!user.groups?.some((group) => group === props.group.id)) {
      const newGroups = [...(user.groups || []), props.group.id] as string[];
      return userStore.save({ ...user, groups: newGroups });
    }
    return Promise.resolve();
  });
  Promise.all(promises)
    .then(() => {
      notifySuccess(t('group.members_added', { count: selected.value.length }));
      emit('saved');
      onHide();
    })
    .catch((error) => {
      notifyError(error);
    });
}

// function onBlur() {
//   setTimeout(() => {
//     showMenu.value = false;
//   }, 200);
// }

function onSearch() {
  results.value = [];
  if (filter.value && filter.value.length > 2) {
    showMenu.value = false;
    loading.value = true;
    userStore
      .searchUsers(filter.value)
      .then((users) => {
        results.value = users || [];
        showMenu.value = true;
      })
      .finally(() => {
        loading.value = false;
      });
  }
}

function onSelectUser(user: UserDto) {
  if (!selected.value.some((u) => u.id === user.id)) {
    selected.value.push(user);
  } else {
    selected.value = selected.value.filter((u) => u.id !== user.id);
  }
  results.value = [];
  filter.value = '';
}
</script>
