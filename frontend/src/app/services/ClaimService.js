const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export const ClaimService = {
  async getAll() {
    const response = await fetch(`${API_BASE_URL}/api/claims`);
    if (!response.ok) {
      throw new Error("Failed to fetch claims");
    }
    return response.json();
  },

  async getById(id) {
    const response = await fetch(`${API_BASE_URL}/api/claims/${id}`);
    if (!response.ok) {
      throw new Error("Failed to fetch claim");
    }
    return response.json();
  },
};
