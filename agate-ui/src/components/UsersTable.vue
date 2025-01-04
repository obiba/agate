<template>
  <div>
    <q-table :rows="users" flat row-key="name" :columns="columns" :pagination="initialPagination">
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
              <q-btn
                v-if="props.row.realm === 'agate-user-realm'"
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :title="t('more_actions')"
                :icon="toolsVisible[props.row.name] ? 'more_vert' : 'none'"
                class="q-ml-xs"
              >
                <q-menu>
                  <q-list style="min-width: 100px">
                    <q-item clickable v-close-popup @click="onShowResetPassword(props.row)">
                      <q-item-section>{{ t('user.reset_password') }}</q-item-section>
                    </q-item>
                    <q-item clickable v-close-popup @click="onShowUpdatePassword(props.row)">
                      <q-item-section>{{ t('user.update_password') }}</q-item-section>
                    </q-item>
                    <q-item v-if="props.row.otpEnabled" clickable v-close-popup @click="onDisableOTP(props.row)">
                      <q-item-section>{{ t('user.disable_2fa') }}</q-item-section>
                    </q-item>
                  </q-list>
                </q-menu>
              </q-btn>
              <q-btn
                v-if="props.row.status === 'PENDING'"
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :title="t('user.approve')"
                :icon="toolsVisible[props.row.name] ? 'done' : 'none'"
                class="q-ml-xs"
                @click="onApprove(props.row)"
              />
              <q-btn
                v-if="props.row.status === 'PENDING'"
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :title="t('user.reject')"
                :icon="toolsVisible[props.row.name] ? 'dangerous' : 'none'"
                class="q-ml-xs"
                @click="onShowReject(props.row)"
              />
            </div>
          </q-td>
          <q-td key="fullName" :props="props"> {{ props.row.firstName }} {{ props.row.lastName }} </q-td>
          <q-td key="email" :props="props">
            <a :href="`mailto:${props.row.email}`">{{ props.row.email }}</a>
          </q-td>
          <q-td key="status" :props="props">
            <span class="text-caption" :title="t(`user.status_hint.${props.row.status}`)">{{
              t(`user.status.${props.row.status}`)
            }}</span>
          </q-td>
          <q-td key="role" :props="props">
            <span class="text-caption">{{ t(`user.role.${props.row.role}`) }}</span>
          </q-td>
          <q-td key="otpEnabled" :props="props">
            <div v-if="props.row.realm === 'agate-user-realm'">
              <q-icon v-if="props.row.otpEnabled" name="radio_button_checked" color="positive" />
              <q-icon v-else name="radio_button_unchecked" color="negative" />
            </div>
          </q-td>
          <q-td key="realm" :props="props">
            <q-chip size="sm">{{ props.row.realm }}</q-chip>
            <q-btn
              v-if="props.row.accountUrl"
              :title="props.row.accountUrl"
              dense
              flat
              icon="account_circle"
              color="secondary"
              size="sm"
              @click="onAccount(props.row)"
            />
          </q-td>
          <q-td key="groups" :props="props">
            <template v-for="grp in props.row.groups" :key="grp">
              <q-badge :label="grp" class="on-left" />
            </template>
          </q-td>
          <q-td key="applications" :props="props">
            <template v-for="app in props.row.applications" :key="app">
              <q-badge :label="applicationStore.getApplicationName(app)" class="on-left" />
            </template>
            <template v-for="(grpApp, idx) in props.row.groupApplications" :key="idx">
              <q-badge
                color="secondary"
                :label="applicationStore.getApplicationName(grpApp.application)"
                :title="t('inherited_from', { parent: grpApp.group })"
                class="on-left"
              />
            </template>
          </q-td>
        </q-tr>
      </template>
    </q-table>
    <confirm-dialog
      v-model="showDelete"
      :title="t('user.remove')"
      :text="t('user.remove_confirm', { name: selected?.name })"
      @confirm="onDelete"
    />
    <confirm-dialog
      v-model="showResetPassword"
      :title="t('user.reset_password')"
      :text="t('user.reset_password_confirm', { name: selected?.name })"
      @confirm="onResetPassword"
    />
    <confirm-dialog
      v-model="showReject"
      :title="t('user.reject')"
      :text="t('user.reject_confirm', { name: selected?.name })"
      @confirm="onDelete"
    />
    <user-dialog v-model="showEdit" :user="selected" @saved="onSaved" />
    <update-password-dialog v-model="showUpdatePassword" :user="selected" />
  </div>
</template>

<script setup lang="ts">
import type { UserDto } from 'src/models/Agate';
import UserDialog from 'src/components/UserDialog.vue';
import UpdatePasswordDialog from 'src/components/UpdatePasswordDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import { DefaultAlignment } from 'src/components/models';
import { notifyError, notifySuccess } from 'src/utils/notify';

const { t } = useI18n();
const userStore = useUserStore();
const groupStore = useGroupStore();
const applicationStore = useApplicationStore();

const filter = ref('');
const toolsVisible = ref<{ [key: string]: boolean }>({});
const initialPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 20,
});
const showEdit = ref(false);
const showDelete = ref(false);
const showResetPassword = ref(false);
const showUpdatePassword = ref(false);
const showReject = ref(false);
const selected = ref();

const users = computed(
  () =>
    userStore.users?.filter((usr) => {
      const str = `${usr.name} ${usr.firstName || ''} ${usr.lastName || ''} ${usr.email}`;
      return filter.value ? str.toLowerCase().includes(filter.value.toLowerCase()) : true;
    }) || [],
);
const columns = computed(() => [
  { name: 'name', label: t('name'), field: 'name', align: DefaultAlignment },
  { name: 'fullName', label: t('fullName'), field: 'fullName', align: DefaultAlignment },
  { name: 'email', label: t('email'), field: 'email', align: DefaultAlignment },
  { name: 'status', label: t('status'), field: 'status', align: DefaultAlignment },
  { name: 'role', label: t('role'), field: 'role', align: DefaultAlignment },
  { name: 'otpEnabled', label: t('otpEnabled'), field: 'otpEnabled', align: DefaultAlignment },
  { name: 'realm', label: t('realm.realm'), field: 'realm', align: DefaultAlignment },
  { name: 'groups', label: t('groups'), field: 'groups', align: DefaultAlignment },
  { name: 'applications', label: t('applications'), field: 'applications', align: DefaultAlignment },
]);

onMounted(() => {
  groupStore.init();
  applicationStore.init();
  refresh();
});

function refresh() {
  userStore.init();
}

function onOverRow(row: UserDto) {
  toolsVisible.value[row.name] = true;
}

function onLeaveRow(row: UserDto) {
  toolsVisible.value[row.name] = false;
}

function onShowEdit(row: UserDto) {
  selected.value = row;
  showEdit.value = true;
}

function onShowDelete(row: UserDto) {
  selected.value = row;
  showDelete.value = true;
}

function onShowResetPassword(row: UserDto) {
  selected.value = row;
  showResetPassword.value = true;
}

function onShowUpdatePassword(row: UserDto) {
  selected.value = row;
  showUpdatePassword.value = true;
}

function onShowReject(row: UserDto) {
  selected.value = row;
  showReject.value = true;
}

function onDelete() {
  userStore
    .remove(selected.value)
    .then(() => notifySuccess(t('user.removed')))
    .catch(() => notifyError(t('user.remove_error')))
    .finally(refresh);
}

function onAdd() {
  selected.value = undefined;
  showEdit.value = true;
}

function onSaved() {
  refresh();
}

function onAccount(row: UserDto) {
  window.open(row.accountUrl, '_blank');
}

function onResetPassword() {
  userStore
    .resetPassword(selected.value)
    .then(() => notifySuccess(t('user.reset_password_success')))
    .catch(() => notifyError(t('user.reset_password_error')));
}

function onApprove(row: UserDto) {
  userStore
    .approve(row)
    .then(() => notifySuccess(t('user.approved')))
    .catch(() => notifyError(t('user.approve_error')))
    .finally(refresh);
}

function onDisableOTP(row: UserDto) {
  userStore
    .disableOTP(row)
    .then(() => notifySuccess(t('user.otp_disabled')))
    .catch(() => notifyError(t('user.otp_disable_error')))
    .finally(refresh);
}
</script>
