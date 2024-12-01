import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { PublicConfigurationDto } from 'src/models/Agate';

export const useSystemStore = defineStore('system', () => {
    const configurationPublic = ref<PublicConfigurationDto | null>(null);

    async function init() {
        return api.get('/config/_public').then((response) => {
            if (response.status === 200) {
                configurationPublic.value = response.data;
            }
            return response;
        });
    }

    return {
        configurationPublic,
        init,
    };
});