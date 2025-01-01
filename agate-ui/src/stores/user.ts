import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { UserDto } from 'src/models/Agate';

export const useUserStore = defineStore('user', () => {
    const users = ref<UserDto[]>([]);

    async function init() {
        return api.get('/users').then((response) => {
            if (response.status === 200) {
                users.value = response.data;
            }
            return response;
        });
    }

    return {
        users,
        init,
    };
});