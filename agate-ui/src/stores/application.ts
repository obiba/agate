import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { ApplicationDto } from 'src/models/Agate';

export const useApplicationStore = defineStore('application', () => {
    const applications = ref<ApplicationDto[]>([]);

    async function init() {
        return api.get('/applications').then((response) => {
            if (response.status === 200) {
                applications.value = response.data;
            }
            return response;
        });
    }

    return {
        applications,
        init,
    };
});