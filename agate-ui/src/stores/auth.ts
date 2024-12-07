import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { SessionDto } from 'src/models/Agate';

export const useAuthStore = defineStore('auth', () => {
  const sid = ref('');
  const version = ref('');
  const session = ref<SessionDto | null>(null);

  const isAdministrator = computed(() => session.value?.role === 'agate-administrator');

  const isAuthenticated = computed(() => {
    return session.value !== null;
  });

  function reset() {
    sid.value = '';
    version.value = '';
    session.value = null;
  }

  async function signin(username: string, password: string, authMethod: string, token: string) {
    const params = new URLSearchParams();
    params.append('username', username);
    params.append('password', password);
    sid.value = '';
    version.value = '';
    const headers = {};
    if (authMethod && token) {
      //headers[authMethod] = token;
    }
    return api.post('/auth/sessions', params, { headers }).then((response) => {
      if (response.status === 201) {
        const sessionUrl = response.headers['location'];
        sid.value = sessionUrl.split('/').pop();
        version.value = response.headers['x-agate-version'];
      }
      return response;
    });
  }

  async function signout() {
    return api.delete('/auth/session/_current').then((response) => {
      reset();
      return response;
    });
  }

  async function userProfile() {
    return api.get('/auth/session/_current').then((response) => {
      if (response.status === 200) {
        version.value = response.headers['x-agate-version'];
        session.value = response.data;
      }
      return response;
    });
  }

  // async function getProviders(): Promise<AuthProviderDto[]> {
  //   return api.get('/auth/providers').then((response) => {
  //     return response.data;
  //   });
  // }

  return {
    sid,
    version,
    session,
    isAuthenticated,
    isAdministrator,
    signin,
    signout,
    userProfile,
    reset,
  };
});
