import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { PublicConfigurationDto, ConfigurationDto } from 'src/models/Agate';

export const useSystemStore = defineStore('system', () => {
  const configurationPublic = ref<PublicConfigurationDto>({} as PublicConfigurationDto);
  const configuration = ref<ConfigurationDto>({} as ConfigurationDto);

  async function initPub() {
    return api.get('/config/_public').then((response) => {
      if (response.status === 200) {
        configurationPublic.value = response.data;
      }
      return response;
    });
  }

  async function init() {
    return api.get('/config').then((response) => {
      if (response.status === 200) {
        configuration.value = response.data;
      }
      return response;
    });
  }

  return {
    configuration,
    configurationPublic,
    init,
    initPub,
  };
});
