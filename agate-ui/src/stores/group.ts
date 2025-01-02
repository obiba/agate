import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { GroupDto } from 'src/models/Agate';

export const useGroupStore = defineStore('group', () => {
  const groups = ref<GroupDto[]>([]);

  async function init() {
    return api.get('/groups').then((response) => {
      groups.value = response.data;
      return response.data;
    });
  }

  async function save(group: GroupDto) {
    return group.id ? api.put(`/group/${group.id}`, group) : api.post('/groups', group);
  }

  async function remove(group: GroupDto) {
    return api.delete(`/group/${group.id}`);
  }

  return {
    groups,
    init,
    remove,
    save,
  };
});
