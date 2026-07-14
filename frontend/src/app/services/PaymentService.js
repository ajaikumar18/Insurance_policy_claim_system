const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export const PaymentService = {
  async getAll() {
    const response = await fetch(`${API_BASE_URL}/api/payments`);
    if (!response.ok) {
      throw new Error("Failed to fetch payments");
    }
    return response.json();
  },

  async create(paymentData) {
    const response = await fetch(`${API_BASE_URL}/api/payments`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(paymentData),
    });
    if (!response.ok) {
      throw new Error("Failed to create payment");
    }
    return response.json();
  },
};
