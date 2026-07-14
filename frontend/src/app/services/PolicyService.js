const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export const PolicyService = {
  async getAll() {
    const response = await fetch(`${API_BASE_URL}/api/policies`);
    if (!response.ok) {
      throw new Error("Failed to fetch policies");
    }
    return response.json();
  },

  async getById(id) {
    const response = await fetch(`${API_BASE_URL}/api/policies/${id}`);
    if (!response.ok) {
      throw new Error("Failed to fetch policy");
    }
    return response.json();
  },
};
