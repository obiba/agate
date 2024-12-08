import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { GroupDto } from 'src/models/Agate';

export const useGroupStore = defineStore('group', () => {
    const groups = ref<GroupDto[]>([]);

    async function init() {
        return api.get('/groups').then((response) => {
            if (response.status === 200) {
                groups.value = response.data;
            }
            return response;
        });
    }

    return {
        groups,
        init,
    };
});