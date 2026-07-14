const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export const UserService = {
  async getAll() {
    const response = await fetch(`${API_BASE_URL}/api/users`);
    if (!response.ok) {
      throw new Error("Failed to fetch users");
    }
    return response.json();
  },

  async getById(id) {
    const response = await fetch(`${API_BASE_URL}/api/users/${id}`);
    if (!response.ok) {
      throw new Error("Failed to fetch user");
    }
    return response.json();
  },
};
