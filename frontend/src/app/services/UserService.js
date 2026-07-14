const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export const UserService = {
  async getAll() {
    const token = localStorage.getItem("ipcms_token");
    const headers = {};
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    const response = await fetch(`${API_BASE_URL}/api/users`, { headers });
    if (!response.ok) {
      throw new Error("Failed to fetch users");
    }
    return response.json();
  },

  async getById(id) {
    const token = localStorage.getItem("ipcms_token");
    const headers = {};
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    const response = await fetch(`${API_BASE_URL}/api/users/${id}`, { headers });
    if (!response.ok) {
      throw new Error("Failed to fetch user");
    }
    return response.json();
  },
};
