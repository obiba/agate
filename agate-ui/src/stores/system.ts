import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { PublicConfigurationDto, ConfigurationDto, AttributeConfigurationDto } from 'src/models/Agate';

export const useSystemStore = defineStore('system', () => {
  const configurationPublic = ref<PublicConfigurationDto>({} as PublicConfigurationDto);
  const configuration = ref<ConfigurationDto>({} as ConfigurationDto);
  const userAttributes = ref<AttributeConfigurationDto[]>([]);

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
        userAttributes.value = configuration.value.userAttributes;
      }
      return response;
    });
  }

  async function addAttribute(attribute: AttributeConfigurationDto) {
    if (attribute) {
      if (!userAttributes.value) {
        configuration.value.userAttributes = [];
        userAttributes.value = configuration.value.userAttributes;
      }

      userAttributes.value.push(attribute);
      return save({ ...configuration.value });
    }
  }

  async function updateAttribute(attribute: AttributeConfigurationDto) {
    if (attribute) {
      const index = userAttributes.value.findIndex(attr => attr.name === attribute.name);
      if (index !== -1) {
        userAttributes.value[index] = attribute;
        return save({ ...configuration.value });
      } else {
        throw new Error('Attribute not found');
      }
    }
  }

  async function removeAttribute(attribute: AttributeConfigurationDto) {
    const index = userAttributes.value.indexOf(attribute);
    if (index !== -1) {
      userAttributes.value.splice(index, 1);
      return save({ ...configuration.value });
    }
  }

  async function save(config: ConfigurationDto) {
    return api.put('/config', config).then((response) => {
      configuration.value = { ...config };
      return response;
    });
  }

  return {
    configuration,
    userAttributes,
    configurationPublic,
    init,
    initPub,
    addAttribute,
    updateAttribute,
    removeAttribute,
    save,
  };
});
