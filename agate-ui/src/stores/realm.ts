import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { RealmConfigDto, RealmConfigSummaryDto } from 'src/models/Agate';
import type { RealmForms } from 'src/components/models';

export const useRealmStore = defineStore('realm', () => {
  const realms = ref<RealmConfigSummaryDto[]>([]);
  const realmForms = ref<RealmForms>();

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
    return realm.status === 'ACTIVE'
      ? api.delete(`/config/realm/${realm.name}/active`)
      : api.put(`/config/realm/${realm.name}/active`);
  }

  async function getConfig(name: string): Promise<RealmConfigDto> {
    return api.get(`/config/realm/${name}`).then((response) => response.data);
  }

  async function save(config: RealmConfigDto) {
    config.name = config.name.trim();
    return config.id ? api.put(`/config/realm/${config.id}`, config) : api.post('/config/realms', config);
  }

  function asJSONObject(json: string | undefined) {
    return json ? JSON.parse(json) : {};
  }

  return {
    realms,
    realmForms,
    init,
    remove,
    toggleActivity,
    getConfig,
    save,
    asJSONObject,
  };
});
