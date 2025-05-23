import { defineStore } from 'pinia';
import { api, baseUrl } from 'src/boot/api';
import type { AuthorizationDto, UserDto } from 'src/models/Agate';

export const useUserStore = defineStore('user', () => {
  const users = ref<UserDto[]>([]);

  async function init() {
    return api.get('/users').then((response) => {
      if (response.status === 200) {
        users.value = response.data.sort((a: UserDto, b: UserDto) => a.name.localeCompare(b.name));
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

  async function updatePassword(user: UserDto, password: string) {
    return api.put(
      `/user/${user.id}/password`,
      { password },
      { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } },
    );
  }

  async function disableOTP(user: UserDto) {
    return api.delete(`/user/${user.id}/otp`);
  }

  async function approve(user: UserDto) {
    return api.put(
      `/user/${user.id}/status`,
      { status: 'approved' },
      { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } },
    );
  }

  async function getAuthorizations(id: string): Promise<AuthorizationDto[]> {
    return api.get(`/user/${id}/authorizations`).then((response) => {
      return response.data;
    });
  }

  async function removeAuthorization(id: string, authorization: AuthorizationDto) {
    return api.delete(`/user/${id}/authorization/${authorization.id}`);
  }

  async function save(user: UserDto, password: string | undefined = undefined) {
    user.name = user.name.trim();
    return user.id ? api.put(`/user/${user.id}`, user) : api.post('/users', { password, user });
  }

  function getUser(id: string | undefined) {
    return users.value?.find((u) => u.id === id);
  }

  async function findUser(nameOfEmail: string): Promise<UserDto | undefined> {
    return api.get('/users/find', { params: { q: nameOfEmail } }).then((response) => {
      return response.data;
    });
  }

  async function searchUsers(nameOfEmail: string): Promise<UserDto[] | undefined> {
    return api.get('/users/search', { params: { q: nameOfEmail } }).then((response) => {
      return response.data;
    });
  }

  function download() {
    window.open(`${baseUrl}/users/_csv`, '_self');
  }

  function generatePassword(length: number = 12): string {
    if (length < 8) {
      throw new Error('Password length should be at least 8 characters for strength.');
    }

    const upperCase = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    const lowerCase = 'abcdefghijklmnopqrstuvwxyz';
    const numbers = '0123456789';
    const specialChars = '@#$%^&+=!';

    const allChars = upperCase + lowerCase + numbers + specialChars;

    const getRandomChar = (chars: string) => chars[Math.floor(Math.random() * chars.length)];

    // Ensure the password contains at least one of each character type
    let password = [
      getRandomChar(upperCase),
      getRandomChar(lowerCase),
      getRandomChar(numbers),
      getRandomChar(specialChars),
    ];

    // Fill the rest of the password length with random characters from all types
    for (let i = password.length; i < length; i++) {
      password.push(getRandomChar(allChars));
    }

    // Shuffle the password to make it more random
    password = password.sort(() => Math.random() - 0.5);

    return password.join('');
  }

  return {
    users,
    init,
    remove,
    resetPassword,
    approve,
    save,
    generatePassword,
    updatePassword,
    disableOTP,
    download,
    getUser,
    findUser,
    searchUsers,
    getAuthorizations,
    removeAuthorization,
  };
});
