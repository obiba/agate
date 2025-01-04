import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { RealmConfigSummaryDto } from 'src/models/Agate';

export const useRealmStore = defineStore('realm', () => {
  const realms = ref<RealmConfigSummaryDto[]>([]);

  async function init() {
    return api.get('/config/realms/summaries').then((response) => {
      if (response.status === 200) {
        realms.value = response.data;
      }
      return response;
    });
  }

  async function remove(realm: RealmConfigSummaryDto) {
    return api.delete(`/config/realm/${realm.name}`);
  }

  async function toggleActivity(realm: RealmConfigSummaryDto) {
    return realm.status === 'ACTIVE' ? api.delete(`/config/realm/${realm.name}/active`) : api.put(`/config/realm/${realm.name}/active`);
  }

  return {
    realms,
    init,
    remove,
    toggleActivity,
  };
});
