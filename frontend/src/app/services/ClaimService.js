const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export const ClaimService = {
  async getAll() {
    const token = localStorage.getItem("ipcms_token");
    const headers = {};
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    const response = await fetch(`${API_BASE_URL}/api/claims`, { headers });
    if (!response.ok) {
      throw new Error("Failed to fetch claims");
    }
    return response.json();
  },

  async registerFnol(fnolData) {
    const token = localStorage.getItem("ipcms_token");
    const headers = { "Content-Type": "application/json" };
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    const response = await fetch(`${API_BASE_URL}/api/claims/fnol`, {
      method: "POST",
      headers,
      body: JSON.stringify(fnolData),
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || "Failed to register FNOL claim");
    }
    return response.json();
  },

  async updateReserve(id, grossReserve) {
    const token = localStorage.getItem("ipcms_token");
    const headers = { "Content-Type": "application/json" };
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    const response = await fetch(`${API_BASE_URL}/api/claims/reserve/${id}`, {
      method: "PUT",
      headers,
      body: JSON.stringify({ grossReserve }),
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || "Failed to update claim reserve");
    }
    return response.json();
  },

  async getById(id) {
    const token = localStorage.getItem("ipcms_token");
    const headers = {};
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    const response = await fetch(`${API_BASE_URL}/api/claims/${id}`, { headers });
    if (!response.ok) {
      throw new Error("Failed to fetch claim");
    }
    return response.json();
  },
};
