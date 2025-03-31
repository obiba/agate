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

  async function save(application: ApplicationDto) {
    application.name = application.name.trim();
    return application.id
      ? api.put(`/application/${application.id}`, application)
      : api.post('/applications', application);
  }

  async function remove(application: ApplicationDto) {
    return api.delete(`/application/${application.id}`);
  }

  function generateKey(length: number = 30): string {
    if (length < 24) {
      throw new Error('Key length should be at least 24 characters for strength.');
    }

    const upperCase = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    const lowerCase = 'abcdefghijklmnopqrstuvwxyz';
    const numbers = '0123456789';

    const allChars = upperCase + lowerCase + numbers;

    const getRandomChar = (chars: string) => chars[Math.floor(Math.random() * chars.length)];

    // Ensure the key contains at least one of each character type
    let key = [getRandomChar(upperCase), getRandomChar(lowerCase), getRandomChar(numbers)];

    // Fill the rest of the key length with random characters from all types
    for (let i = key.length; i < length; i++) {
      key.push(getRandomChar(allChars));
    }

    // Shuffle the key to make it more random
    key = key.sort(() => Math.random() - 0.5);

    return key.join('');
  }

  function getApplicationName(id: string | undefined) {
    return applications.value?.find((app) => app.id === id)?.name || id || '';
  }

  return {
    applications,
    init,
    save,
    remove,
    generateKey,
    getApplicationName,
  };
});
