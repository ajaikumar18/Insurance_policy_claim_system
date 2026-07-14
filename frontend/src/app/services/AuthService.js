const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export const AuthService = {
  async login(email, password) {
    return fetch(`${API_BASE_URL}/api/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    }).then(async (response) => {
      const data = await response.json().catch(() => ({}));
      if (!response.ok) {
        throw new Error(data.message || "Login failed");
      }
      return data;
    });
  },

  async logout() {
    return fetch(`${API_BASE_URL}/api/auth/logout`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
    }).then(async (response) => {
      const data = await response.json().catch(() => ({}));
      if (!response.ok) {
        throw new Error(data.message || "Logout failed");
      }
      return data;
    });
  },
};
