const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export const UnderwritingService = {
  async getQuote(regionScore, assetAgeScore, priorClaimsScore, businessTypeScore) {
    const token = localStorage.getItem("ipcms_token");
    const headers = { "Content-Type": "application/json" };
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    const response = await fetch(`${API_BASE_URL}/api/underwrite/quote`, {
      method: "POST",
      headers,
      body: JSON.stringify({ regionScore, assetAgeScore, priorClaimsScore, businessTypeScore }),
    });
    if (!response.ok) {
      throw new Error("Failed to calculate premium quote");
    }
    return response.json();
  },

  async submitOverride(policyNumber, overrideType, deltaValue) {
    const token = localStorage.getItem("ipcms_token");
    const headers = { "Content-Type": "application/json" };
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    const response = await fetch(`${API_BASE_URL}/api/underwrite/override`, {
      method: "POST",
      headers,
      body: JSON.stringify({ policyNumber, overrideType, deltaValue }),
    });
    if (!response.ok) {
      throw new Error("Failed to submit override request");
    }
    return response.json();
  },

  async getAllOverrides() {
    const token = localStorage.getItem("ipcms_token");
    const headers = {};
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    const response = await fetch(`${API_BASE_URL}/api/underwrite/overrides`, { headers });
    if (!response.ok) {
      throw new Error("Failed to fetch overrides");
    }
    return response.json();
  },

  async approveOverride(id) {
    const token = localStorage.getItem("ipcms_token");
    const headers = {};
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    const response = await fetch(`${API_BASE_URL}/api/underwrite/override/${id}/approve`, {
      method: "PUT",
      headers,
    });
    if (!response.ok) {
      throw new Error("Failed to approve override request");
    }
    return response.json();
  },

  async rejectOverride(id) {
    const token = localStorage.getItem("ipcms_token");
    const headers = {};
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    const response = await fetch(`${API_BASE_URL}/api/underwrite/override/${id}/reject`, {
      method: "PUT",
      headers,
    });
    if (!response.ok) {
      throw new Error("Failed to reject override request");
    }
    return response.json();
  },
};
