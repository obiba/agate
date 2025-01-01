<template>
  <div>
    <div v-if="authStore.isAuthenticated" class="q-mt-none q-mb-none q-pa-md">
      <span class="text-bold text-grey-6">{{ username }}</span>
    </div>
    <q-list>
      <q-item clickable @click="onProfile" v-if="authStore.isAuthenticated">
        <q-item-section avatar>
          <q-icon name="person" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ t('my_profile') }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-item clickable @click="onSignout" v-if="authStore.isAuthenticated">
        <q-item-section avatar>
          <q-icon name="logout" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ t('auth.signout') }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-separator v-if="authStore.isAuthenticated" />
      <q-item to="/users">
        <q-item-section avatar>
          <q-icon name="person" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ t('users') }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-item to="/groups">
        <q-item-section avatar>
          <q-icon name="people" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ t('groups') }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-item :to="`/applications`">
        <q-item-section avatar>
          <q-icon name="splitscreen" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ t('applications') }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-item :to="`/realms`">
        <q-item-section avatar>
          <q-icon name="recent_actors" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ t('realms') }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-item v-if="authStore.isAdministrator" :to="`/settings`">
        <q-item-section avatar>
          <q-icon name="settings" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ t('settings') }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-item-label header>{{ t('other_links') }}</q-item-label>
      <EssentialLink v-for="link in essentialLinks" :key="link.title" v-bind="link" />
      <q-item class="fixed-bottom text-caption">
        <div>
          {{ t('main.powered_by') }}
          <a class="text-weight-bold" href="https://www.obiba.org/pages/products/agate" target="_blank">OBiBa Agate</a>
          <span class="q-ml-xs" style="font-size: smaller">{{ authStore.version }}</span>
        </div>
      </q-item>
    </q-list>
  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'MainDrawer',
});
</script>
<script setup lang="ts">
import EssentialLink from 'components/EssentialLink.vue';
import type { EssentialLinkProps } from 'components/EssentialLink.vue';

const { t } = useI18n();
const authStore = useAuthStore();

const username = computed(() => authStore.session?.username || '?');

const essentialLinks: EssentialLinkProps[] = [
  {
    title: t('docs'),
    caption: t('documentation_cookbook'),
    icon: 'school',
    link: 'https://agatedoc.obiba.org',
  },
  {
    title: t('source_code'),
    caption: 'github.com/obiba/agate',
    icon: 'code',
    link: 'https://github.com/obiba/agate',
  },
];

function onProfile() {
  window.location.href = '../profile';
}

function onSignout() {
  window.location.href = '../signout';
}
</script>
