const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export const PolicyService = {
  async getAll() {
    const token = localStorage.getItem("ipcms_token");
    const headers = {};
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    const response = await fetch(`${API_BASE_URL}/api/policies`, { headers });
    if (!response.ok) {
      throw new Error("Failed to fetch policies");
    }
    return response.json();
  },

  async getById(id) {
    const token = localStorage.getItem("ipcms_token");
    const headers = {};
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    const response = await fetch(`${API_BASE_URL}/api/policies/${id}`, { headers });
    if (!response.ok) {
      throw new Error("Failed to fetch policy");
    }
    return response.json();
  },

  async endorsePolicy(id, endorsementData) {
    const token = localStorage.getItem("ipcms_token");
    const headers = { "Content-Type": "application/json" };
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    const response = await fetch(`${API_BASE_URL}/api/policies/${id}/endorse`, {
      method: "POST",
      headers,
      body: JSON.stringify(endorsementData)
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || "Failed to endorse policy");
    }
    return response.json();
  },

  async getPolicyHistory(id) {
    const token = localStorage.getItem("ipcms_token");
    const headers = {};
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    const response = await fetch(`${API_BASE_URL}/api/policies/${id}/history`, { headers });
    if (!response.ok) {
      throw new Error("Failed to fetch policy history");
    }
    return response.json();
  }
};
