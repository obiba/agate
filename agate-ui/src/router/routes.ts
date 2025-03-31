import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  { path: '/index.html', redirect: '/' },
  {
    path: '/',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      { path: '', component: () => import('pages/IndexPage.vue') },
      { path: 'users', component: () => import('pages/UsersPage.vue') },
      { path: 'user/:id', component: () => import('pages/UserPage.vue') },
      { path: 'groups', component: () => import('pages/GroupsPage.vue') },
      { path: 'group/:id', component: () => import('pages/GroupPage.vue') },
      { path: 'applications', component: () => import('pages/ApplicationsPage.vue') },
      { path: 'application/:id', component: () => import('pages/ApplicationPage.vue') },
      { path: 'realms', component: () => import('pages/RealmsPage.vue') },
      { path: 'tickets', component: () => import('pages/TicketsPage.vue') },
      { path: 'settings', component: () => import('pages/SettingsPage.vue') },
    ],
  },

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/ErrorNotFound.vue'),
  },
];

export default routes;
