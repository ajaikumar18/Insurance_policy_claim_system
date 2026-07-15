const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export const AdminService = {
  async getAuditLogs() {
    const token = localStorage.getItem("ipcms_token");
    const headers = {};
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    const response = await fetch(`${API_BASE_URL}/api/admin/audit-logs`, { headers });
    if (!response.ok) {
      throw new Error("Failed to fetch audit logs");
    }
    return response.json();
  },

  async getUsers() {
    const token = localStorage.getItem("ipcms_token");
    const headers = {};
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    const response = await fetch(`${API_BASE_URL}/api/admin/users`, { headers });
    if (!response.ok) {
      throw new Error("Failed to fetch users");
    }
    return response.json();
  },

  async createUser(userData) {
    const token = localStorage.getItem("ipcms_token");
    const headers = {
      "Content-Type": "application/json"
    };
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    const response = await fetch(`${API_BASE_URL}/api/admin/users`, {
      method: "POST",
      headers,
      body: JSON.stringify(userData)
    });
    if (!response.ok) {
      const err = await response.json().catch(() => ({}));
      throw new Error(err.message || "Failed to create user");
    }
    return response.json();
  }
};
