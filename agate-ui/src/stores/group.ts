import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { GroupDto, UserSummaryDto } from 'src/models/Agate';

export const useGroupStore = defineStore('group', () => {
  const groups = ref<GroupDto[]>([]);

  async function init() {
    return api.get('/groups').then((response) => {
      groups.value = response.data.sort((a: GroupDto, b: GroupDto) => a.name.localeCompare(b.name));
      return response.data;
    });
  }

  async function save(group: GroupDto) {
    group.name = group.name.trim();
    return group.id ? api.put(`/group/${group.id}`, group) : api.post('/groups', group);
  }

  async function remove(group: GroupDto) {
    return api.delete(`/group/${group.id}`);
  }

  async function getUsers(groupId: string): Promise<UserSummaryDto[]> {
    return api.get(`/group/${groupId}/users`).then((response) => response.data);
  }

  async function removeUsers(groupId: string, names: string[]): Promise<void> {
    return api.delete(`/group/${groupId}/users`, {
      params: { names },
      paramsSerializer: {
        indexes: null, // no brackets at all
      },
    });
  }

  function getGroupName(id: string | undefined) {
    return groups.value?.find((g) => g.id === id)?.name || id || '';
  }

  function getGroup(id: string | undefined) {
    return groups.value?.find((g) => g.id === id);
  }

  return {
    groups,
    init,
    remove,
    save,
    getGroupName,
    getGroup,
    getUsers,
    removeUsers,
  };
});
