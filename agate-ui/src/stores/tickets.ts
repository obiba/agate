import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { TicketDto } from 'src/models/Agate';

export const useTicketStore = defineStore('ticket', () => {
  const tickets = ref<TicketDto[]>([]);

  async function init() {
    return api.get('/tickets').then((response) => {
      if (response.status === 200) {
        tickets.value = response.data;
      }
      return response;
    });
  }

  async function remove(ticket: TicketDto) {
    return api.delete(`/ticket/${ticket.id}`);
  }

  return {
    tickets,
    init,
    remove,
  };
});
