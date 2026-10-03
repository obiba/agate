import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type {
  PublicConfigurationDto,
  ConfigurationDto,
  AttributeConfigurationDto,
  LocalizedStringDto,
} from 'src/models/Agate';

export interface NotificationTemplate {
  name: string;
  bundled: boolean;
  custom: boolean;
  // fallback folder, when the template is only there
  inheritedFrom?: string;
}

export const useSystemStore = defineStore('system', () => {
  const configurationPublic = ref<PublicConfigurationDto>({} as PublicConfigurationDto);
  const configuration = ref<ConfigurationDto>({} as ConfigurationDto);
  const userAttributes = ref<AttributeConfigurationDto[]>([]);
  const translations = ref<LocalizedStringDto[]>([]);
  const defaultLanguage = computed(() => (configuration.value.languages || [])[0] || 'en');

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
        userAttributes.value = configuration.value.userAttributes || [];
        translations.value = configuration.value.translations;
      }
      return response;
    });
  }

  async function addAttribute(attribute: AttributeConfigurationDto) {
    if (attribute) {
      if (!configuration.value.userAttributes) {
        configuration.value.userAttributes = [];
        userAttributes.value = configuration.value.userAttributes;
      }

      userAttributes.value.push(attribute);
      return save({ ...configuration.value });
    }
  }

  async function updateAttribute(attribute: AttributeConfigurationDto) {
    if (attribute) {
      const index = userAttributes.value.findIndex((attr) => attr.name === attribute.name);
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

  async function updateTranslation(newTranslations: LocalizedStringDto[]) {
    if (newTranslations && newTranslations.length) {
      if (!configuration.value.translations) {
        configuration.value.translations = [];
        translations.value = configuration.value.translations;
      }

      translations.value.splice(0, newTranslations.length, ...newTranslations);
      return save({ ...configuration.value });
    }
  }

  async function save(config: ConfigurationDto) {
    return api.put('/config', config).then((response) => {
      configuration.value = { ...config };
      return response;
    });
  }

  async function getNotificationTemplates(folder?: string): Promise<NotificationTemplate[]> {
    return api.get('/config/notification-templates', { params: { folder } }).then((response) => response.data);
  }

  async function getNotificationTemplate(name: string, folder?: string): Promise<string> {
    return api
      .get(`/config/notification-templates/${name}`, { params: { folder }, responseType: 'text' })
      .then((response) => response.data);
  }

  async function saveNotificationTemplate(name: string, content: string, folder?: string) {
    return api.put(`/config/notification-templates/${name}`, content, {
      params: { folder },
      headers: { 'Content-Type': 'text/plain' },
    });
  }

  async function previewNotificationTemplate(name: string, content: string, folder?: string): Promise<string> {
    return api
      .post(`/config/notification-templates/${name}/_preview`, content, {
        params: { folder },
        headers: { 'Content-Type': 'text/plain' },
        responseType: 'text',
      })
      .then((response) => response.data);
  }

  async function deleteNotificationTemplate(name: string, folder?: string) {
    return api.delete(`/config/notification-templates/${name}`, { params: { folder } });
  }

  return {
    configuration,
    userAttributes,
    translations,
    configurationPublic,
    defaultLanguage,
    init,
    initPub,
    addAttribute,
    updateAttribute,
    removeAttribute,
    updateTranslation,
    save,
    getNotificationTemplates,
    getNotificationTemplate,
    saveNotificationTemplate,
    previewNotificationTemplate,
    deleteNotificationTemplate,
  };
});
