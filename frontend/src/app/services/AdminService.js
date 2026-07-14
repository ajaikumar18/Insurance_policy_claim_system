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
  }
};
