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

  async function remove(user: UserDto) {
    return api.delete(`/user/${user.id}`);
  }

  async function resetPassword(user: UserDto) {
    return api.put(`/user/${user.id}/reset_password`);
  }

  async function approve(user: UserDto) {
    return api.put(
      `/user/${user.id}/status`,
      { status: 'approved' },
      { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } },
    );
  }

  async function save(user: UserDto) {
    user.name = user.name.trim();
    return user.id ? api.put(`/user/${user.id}`, user) : api.post('/users', { user });
  }

  return {
    users,
    init,
    remove,
    resetPassword,
    approve,
    save,
  };
});
