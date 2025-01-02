<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="row q-col-gutter-lg q-mb-lg">
        <div class="col-8 col-sm-8 col-xs-12">
          <q-item-label header class="text-uppercase">{{ t('content_management') }}</q-item-label>
          <div class="row">
            <q-card flat bordered class="on-left q-mb-md o-card-sm bg-grey-1">
              <q-card-section class="q-pa-sm text-h6 text-center bg-grey-4">
                <router-link to="/users">{{ t('users') }}</router-link>
              </q-card-section>
              <q-separator />
              <q-card-section class="text-hint">
                {{ t('users_caption') }}
                <q-btn flat rounded dense icon="arrow_forward" size="xs" color="primary" :to="`/users`" />
              </q-card-section>
            </q-card>
            <q-card flat bordered class="on-left q-mb-md o-card-sm bg-grey-1">
              <q-card-section class="q-pa-sm text-h6 text-center bg-grey-4">
                <router-link to="/groups">{{ t('groups') }}</router-link>
              </q-card-section>
              <q-separator />
              <q-card-section class="text-hint">
                {{ t('groups_caption') }}
                <q-btn flat rounded dense icon="arrow_forward" size="xs" color="primary" :to="`/groups`" />
              </q-card-section>
            </q-card>
            <q-card flat bordered class="on-left q-mb-md o-card-sm bg-grey-1">
              <q-card-section class="q-pa-sm text-h6 text-center bg-grey-4">
                <router-link to="/applications">{{ t('applications') }}</router-link>
              </q-card-section>
              <q-separator />
              <q-card-section class="text-hint">
                {{ t('applications_caption') }}
                <q-btn flat rounded dense icon="arrow_forward" size="xs" color="primary" :to="`/applications`" />
              </q-card-section>
            </q-card>
            <q-card flat bordered class="on-left q-mb-md o-card-sm bg-grey-1">
              <q-card-section class="q-pa-sm text-h6 text-center bg-grey-4">
                <router-link to="/realms">{{ t('realms') }}</router-link>
              </q-card-section>
              <q-separator />
              <q-card-section class="text-hint">
                {{ t('realms_caption') }}
                <q-btn flat rounded dense icon="arrow_forward" size="xs" color="primary" :to="`/realms`" />
              </q-card-section>
            </q-card>
          </div>
        </div>
        <div class="col-4 col-sm-4 col-xs-12"></div>
      </div>
    </q-page>
  </div>
</template>

<script setup lang="ts">
const { t } = useI18n();
//const authStore = useAuthStore();
</script>
