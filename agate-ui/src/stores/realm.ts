import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { RealmConfigDto } from 'src/models/Agate';

export const useRealmStore = defineStore('realm', () => {
    const realms = ref<RealmConfigDto[]>([]);

    async function init() {
        return api.get('/config/realms/summaries').then((response) => {
            if (response.status === 200) {
                realms.value = response.data;
            }
            return response;
        });
    }

    return {
        realms,
        init,
    };
});