
const API_BASE_URL = import.meta.env.PROD
  ? 'https://your-api-domain.com/api/v1'
  : 'http://localhost:3000/api/v1';

class APIClient {
  private baseURL: string;
  private token: string | null = null;

  constructor() {
    this.baseURL = API_BASE_URL;
    this.token = localStorage.getItem('admin_token');
  }

  setToken(token: string) {
    this.token = token;
    localStorage.setItem('admin_token', token);
  }

  clearToken() {
    this.token = null;
    localStorage.removeItem('admin_token');
  }

  private async request(endpoint: string, options: RequestInit = {}) {
    const url = `${this.baseURL}${endpoint}`;
    const config: RequestInit = {
      headers: {
        'Content-Type': 'application/json',
        ...(this.token && { Authorization: `Bearer ${this.token}` }),
        ...options.headers,
      },
      ...options,
    };

    const response = await fetch(url, config);
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return response.json();
  }

  // Auth - Fixed admin login route
  async login(email: string, password: string) {
    const response = await this.request('/auth/admin/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
    
    if (response.success) {
      this.setToken(response.token);
    }
    
    return response;
  }

  async logout() {
    this.clearToken();
  }

  // Dashboard
  async getDashboardStats() {
    return this.request('/admin/stats');
  }

  async getRecentActivity() {
    return this.request('/admin/activity');
  }

  // Users
  async getUsers(page = 1, limit = 10, search = '') {
    const params = new URLSearchParams({ 
      page: page.toString(), 
      limit: limit.toString(),
      ...(search && { search })
    });
    return this.request(`/users?${params}`);
  }

  async updateUserStatus(userId: number, status: string) {
    return this.request(`/users/${userId}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status }),
    });
  }

  // Servers
  async getServers() {
    return this.request('/servers');
  }

  async createServer(serverData: Record<string, unknown>) {
    return this.request('/servers', {
      method: 'POST',
      body: JSON.stringify(serverData),
    });
  }

  async updateServer(serverId: number, serverData: Record<string, unknown>) {
    return this.request(`/servers/${serverId}`, {
      method: 'PUT',
      body: JSON.stringify(serverData),
    });
  }

  async deleteServer(serverId: number) {
    return this.request(`/servers/${serverId}`, {
      method: 'DELETE',
    });
  }

  // Payments - Fixed route to match backend
  async getPendingPayments() {
    return this.request('/subscription/pending');
  }

  async approvePayment(paymentId: number, adminNotes?: string) {
    return this.request(`/subscription/${paymentId}/approve`, {
      method: 'POST',
      body: JSON.stringify({ admin_notes: adminNotes }),
    });
  }

  async rejectPayment(paymentId: number, adminNotes?: string) {
    return this.request(`/subscription/${paymentId}/reject`, {
      method: 'POST',
      body: JSON.stringify({ admin_notes: adminNotes }),
    });
  }

  // Notifications
  async sendNotification(notificationData: Record<string, unknown>) {
    return this.request('/notifications/send', {
      method: 'POST',
      body: JSON.stringify(notificationData),
    });
  }

  async getAllNotifications() {
    return this.request('/notifications/admin/all');
  }

  // Ad Configuration
  async getAdConfig() {
    return this.request('/ads/admin/config');
  }

  async updateAdConfig(adData: Record<string, unknown>) {
    return this.request('/ads/admin/config', {
      method: 'PUT',
      body: JSON.stringify(adData),
    });
  }

  // OneConnect
  async getOneConnectConfig() {
    return this.request('/oneconnect/config');
  }

  async updateOneConnectConfig(configData: Record<string, unknown>) {
    return this.request('/oneconnect/config', {
      method: 'PUT',
      body: JSON.stringify(configData),
    });
  }

  async syncOneConnectServers() {
    return this.request('/oneconnect/sync', {
      method: 'POST',
    });
  }

  // Support
  async getSupportTickets(status?: string) {
    const params = status ? `?status=${status}` : '';
    return this.request(`/support/admin/tickets${params}`);
  }

  async updateSupportTicket(ticketId: number, ticketData: Record<string, unknown>) {
    return this.request(`/support/admin/tickets/${ticketId}`, {
      method: 'PUT',
      body: JSON.stringify(ticketData),
    });
  }

  // Blog
  async getBlogPosts() {
    return this.request('/blog/admin/posts');
  }

  async createBlogPost(postData: Record<string, unknown>) {
    return this.request('/blog/admin/posts', {
      method: 'POST',
      body: JSON.stringify(postData),
    });
  }

  async updateBlogPost(postId: number, postData: Record<string, unknown>) {
    return this.request(`/blog/admin/posts/${postId}`, {
      method: 'PUT',
      body: JSON.stringify(postData),
    });
  }

  async deleteBlogPost(postId: number) {
    return this.request(`/blog/admin/posts/${postId}`, {
      method: 'DELETE',
    });
  }

  // Rewards
  async getAllRewards() {
    return this.request('/rewards/all');
  }

  // Leaderboard
  async getPointsLeaderboard() {
    return this.request('/leaderboard/points');
  }

  async getReferralLeaderboard() {
    return this.request('/leaderboard/referrals');
  }
}

export const apiClient = new APIClient();
