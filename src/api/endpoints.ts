// API Endpoints for Android VPN App Integration
// These endpoints should be implemented in your backend (Laravel/Node.js/etc.)

export const API_BASE_URL = 'https://your-api-domain.com/api/v1';

// Authentication Endpoints
export const AUTH_ENDPOINTS = {
  LOGIN: `${API_BASE_URL}/auth/login`,
  REGISTER: `${API_BASE_URL}/auth/register`,
  LOGOUT: `${API_BASE_URL}/auth/logout`,
  REFRESH_TOKEN: `${API_BASE_URL}/auth/refresh`,
  FORGOT_PASSWORD: `${API_BASE_URL}/auth/forgot-password`,
  RESET_PASSWORD: `${API_BASE_URL}/auth/reset-password`,
  VERIFY_EMAIL: `${API_BASE_URL}/auth/verify-email`,
};

// User Management
export const USER_ENDPOINTS = {
  PROFILE: `${API_BASE_URL}/user/profile`,
  UPDATE_PROFILE: `${API_BASE_URL}/user/update`,
  DELETE_ACCOUNT: `${API_BASE_URL}/user/delete`,
  BLOCK_USER: `${API_BASE_URL}/admin/users/{id}/block`,
  UNBLOCK_USER: `${API_BASE_URL}/admin/users/{id}/unblock`,
  GET_USERS: `${API_BASE_URL}/admin/users`,
  USER_STATS: `${API_BASE_URL}/user/stats`,
};

// Server Management
export const SERVER_ENDPOINTS = {
  GET_SERVERS: `${API_BASE_URL}/servers`,
  GET_FREE_SERVERS: `${API_BASE_URL}/servers/free`,
  GET_PREMIUM_SERVERS: `${API_BASE_URL}/servers/premium`,
  ADD_SERVER: `${API_BASE_URL}/admin/servers`,
  UPDATE_SERVER: `${API_BASE_URL}/admin/servers/{id}`,
  DELETE_SERVER: `${API_BASE_URL}/admin/servers/{id}`,
  SERVER_STATUS: `${API_BASE_URL}/servers/{id}/status`,
  CONNECT_SERVER: `${API_BASE_URL}/servers/{id}/connect`,
  DISCONNECT_SERVER: `${API_BASE_URL}/servers/{id}/disconnect`,
};

// OneConnect VPN Integration
export const ONECONNECT_ENDPOINTS = {
  // Configuration
  SAVE_CONFIG: `${API_BASE_URL}/admin/oneconnect/config`,
  GET_CONFIG: `${API_BASE_URL}/admin/oneconnect/config`,
  TEST_CONNECTION: `${API_BASE_URL}/admin/oneconnect/test`,
  
  // Service Control
  ENABLE_SERVICE: `${API_BASE_URL}/admin/oneconnect/enable`,
  DISABLE_SERVICE: `${API_BASE_URL}/admin/oneconnect/disable`,
  GET_SERVICE_STATUS: `${API_BASE_URL}/admin/oneconnect/status`,
  
  // Server Management
  SYNC_SERVERS: `${API_BASE_URL}/admin/oneconnect/sync`,
  GET_SERVERS: `${API_BASE_URL}/servers/oneconnect`,
  GET_SERVER_CONFIG: `${API_BASE_URL}/oneconnect/servers/{id}/config`,
  CONNECT_SERVER: `${API_BASE_URL}/oneconnect/servers/{id}/connect`,
  DISCONNECT_SERVER: `${API_BASE_URL}/oneconnect/servers/{id}/disconnect`,
};

// Subscription & Payments
export const SUBSCRIPTION_ENDPOINTS = {
  GET_PLANS: `${API_BASE_URL}/subscription/plans`,
  SUBSCRIBE: `${API_BASE_URL}/subscription/subscribe`,
  CANCEL_SUBSCRIPTION: `${API_BASE_URL}/subscription/cancel`,
  SUBSCRIPTION_STATUS: `${API_BASE_URL}/subscription/status`,
  PAYMENT_HISTORY: `${API_BASE_URL}/subscription/payments`,
  VERIFY_PURCHASE: `${API_BASE_URL}/subscription/verify-purchase`,
};

// Points & Rewards System
export const REWARDS_ENDPOINTS = {
  GET_POINTS: `${API_BASE_URL}/rewards/points`,
  ADD_POINTS: `${API_BASE_URL}/rewards/add-points`,
  REDEEM_POINTS: `${API_BASE_URL}/rewards/redeem`,
  DAILY_CHECKIN: `${API_BASE_URL}/rewards/daily-checkin`,
  WATCH_VIDEO_REWARD: `${API_BASE_URL}/rewards/watch-video`,
  REFER_FRIEND: `${API_BASE_URL}/rewards/refer-friend`,
  SHARE_APP_REWARD: `${API_BASE_URL}/rewards/share-app`,
  RATE_APP_REWARD: `${API_BASE_URL}/rewards/rate-app`,
  LEADERBOARD: `${API_BASE_URL}/rewards/leaderboard`,
  PAYOUT_REQUEST: `${API_BASE_URL}/rewards/payout-request`,
};

// Games & Activities
export const GAME_ENDPOINTS = {
  LUCKY_WHEEL_SPIN: `${API_BASE_URL}/games/lucky-wheel/spin`,
  TIC_TAC_TOE_START: `${API_BASE_URL}/games/tic-tac-toe/start`,
  TIC_TAC_TOE_MOVE: `${API_BASE_URL}/games/tic-tac-toe/move`,
  SCRATCH_CARD_PLAY: `${API_BASE_URL}/games/scratch-card/play`,
  GAME_HISTORY: `${API_BASE_URL}/games/history`,
};

// Enhanced Ad Management with Multiple Networks
export const AD_ENDPOINTS = {
  GET_AD_CONFIG: `${API_BASE_URL}/ads/config`,
  SAVE_AD_CONFIG: `${API_BASE_URL}/admin/ads/config`,
  RECORD_AD_VIEW: `${API_BASE_URL}/ads/view`,
  RECORD_AD_CLICK: `${API_BASE_URL}/ads/click`,
  GET_REWARDED_AD: `${API_BASE_URL}/ads/rewarded`,
  GET_AD_NETWORKS: `${API_BASE_URL}/admin/ads/networks`,
  UPDATE_AD_NETWORK: `${API_BASE_URL}/admin/ads/networks/{network}`,
  GET_AD_STATISTICS: `${API_BASE_URL}/admin/ads/statistics`,
};

// App Settings
export const SETTINGS_ENDPOINTS = {
  GET_APP_CONFIG: `${API_BASE_URL}/app/config`,
  CHECK_UPDATE: `${API_BASE_URL}/app/check-update`,
  GET_MAINTENANCE_STATUS: `${API_BASE_URL}/app/maintenance-status`,
  GET_BANNER_IMAGE: `${API_BASE_URL}/app/banner`,
  GET_PRIVACY_POLICY: `${API_BASE_URL}/app/privacy-policy`,
  GET_TERMS_OF_SERVICE: `${API_BASE_URL}/app/terms`,
};

// Analytics & Reporting
export const ANALYTICS_ENDPOINTS = {
  TRACK_EVENT: `${API_BASE_URL}/analytics/track`,
  APP_USAGE: `${API_BASE_URL}/analytics/usage`,
  CONNECTION_LOGS: `${API_BASE_URL}/analytics/connections`,
  USER_ACTIVITY: `${API_BASE_URL}/analytics/user-activity`,
};

// Support & Feedback
export const SUPPORT_ENDPOINTS = {
  SUBMIT_FEEDBACK: `${API_BASE_URL}/support/feedback`,
  GET_FAQ: `${API_BASE_URL}/support/faq`,
  CREATE_TICKET: `${API_BASE_URL}/support/ticket`,
  GET_TICKETS: `${API_BASE_URL}/support/tickets`,
  GET_ALL_TICKETS: `${API_BASE_URL}/admin/support/tickets`,
  UPDATE_TICKET_STATUS: `${API_BASE_URL}/admin/support/tickets/{id}/status`,
  REPLY_TO_TICKET: `${API_BASE_URL}/admin/support/tickets/{id}/reply`,
};

// Notification System Endpoints
export const NOTIFICATION_ENDPOINTS = {
  SEND_BROADCAST: `${API_BASE_URL}/admin/notifications/broadcast`,
  SEND_INDIVIDUAL: `${API_BASE_URL}/admin/notifications/individual`,
  GET_NOTIFICATION_HISTORY: `${API_BASE_URL}/admin/notifications/history`,
  GET_USER_NOTIFICATIONS: `${API_BASE_URL}/notifications`,
  MARK_AS_READ: `${API_BASE_URL}/notifications/{id}/read`,
  GET_UNREAD_COUNT: `${API_BASE_URL}/notifications/unread-count`,
};

// Support System Endpoints
export const BLOG_ENDPOINTS = {
  GET_POSTS: `${API_BASE_URL}/blog/posts`,
  GET_POST: `${API_BASE_URL}/blog/posts/{id}`,
  CREATE_POST: `${API_BASE_URL}/admin/blog/posts`,
  UPDATE_POST: `${API_BASE_URL}/admin/blog/posts/{id}`,
  DELETE_POST: `${API_BASE_URL}/admin/blog/posts/{id}`,
  PUBLISH_POST: `${API_BASE_URL}/admin/blog/posts/{id}/publish`,
  GET_CATEGORIES: `${API_BASE_URL}/blog/categories`,
  SEARCH_POSTS: `${API_BASE_URL}/blog/posts/search`,
  INCREMENT_VIEWS: `${API_BASE_URL}/blog/posts/{id}/view`,
};

// Leaderboard Endpoints
export const LEADERBOARD_ENDPOINTS = {
  GET_LEADERBOARD: `${API_BASE_URL}/leaderboard`,
  GET_USER_RANK: `${API_BASE_URL}/leaderboard/user/{id}`,
  GET_TOP_USERS: `${API_BASE_URL}/leaderboard/top/{limit}`,
  GET_LEADERBOARD_BY_PERIOD: `${API_BASE_URL}/leaderboard/{period}`, // daily, weekly, monthly, all-time
  GET_ACHIEVEMENTS: `${API_BASE_URL}/achievements`,
  GET_USER_ACHIEVEMENTS: `${API_BASE_URL}/achievements/user/{id}`,
  REWARD_TOP_USERS: `${API_BASE_URL}/admin/leaderboard/reward-top`,
  RESET_LEADERBOARD: `${API_BASE_URL}/admin/leaderboard/reset`,
};

// Example API Request Functions
export const apiService = {
  // Authentication
  async login(email: string, password: string) {
    const response = await fetch(AUTH_ENDPOINTS.LOGIN, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email, password }),
    });
    return response.json();
  },

  // Get servers
  async getServers(type?: 'free' | 'premium' | 'oneconnect') {
    let url = SERVER_ENDPOINTS.GET_SERVERS;
    if (type === 'free') url = SERVER_ENDPOINTS.GET_FREE_SERVERS;
    else if (type === 'premium') url = SERVER_ENDPOINTS.GET_PREMIUM_SERVERS;
    else if (type === 'oneconnect') url = ONECONNECT_ENDPOINTS.GET_SERVERS;
    
    const response = await fetch(url, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
    });
    return response.json();
  },

  // OneConnect Configuration
  async saveOneConnectConfig(apiKey: string, apiUrl: string) {
    const response = await fetch(ONECONNECT_ENDPOINTS.SAVE_CONFIG, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
      body: JSON.stringify({ 
        api_key: apiKey, 
        api_url: apiUrl,
        enabled: true 
      }),
    });
    return response.json();
  },

  async getOneConnectConfig() {
    const response = await fetch(ONECONNECT_ENDPOINTS.GET_CONFIG, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
    });
    return response.json();
  },

  async testOneConnectConnection(apiKey: string, apiUrl: string) {
    const response = await fetch(ONECONNECT_ENDPOINTS.TEST_CONNECTION, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
      body: JSON.stringify({ api_key: apiKey, api_url: apiUrl }),
    });
    return response.json();
  },

  // OneConnect Service Control
  async enableOneConnectService() {
    const response = await fetch(ONECONNECT_ENDPOINTS.ENABLE_SERVICE, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
    });
    return response.json();
  },

  async disableOneConnectService() {
    const response = await fetch(ONECONNECT_ENDPOINTS.DISABLE_SERVICE, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
    });
    return response.json();
  },

  async getOneConnectServiceStatus() {
    const response = await fetch(ONECONNECT_ENDPOINTS.GET_SERVICE_STATUS, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
    });
    return response.json();
  },

  // OneConnect Server Management
  async syncOneConnectServers() {
    const response = await fetch(ONECONNECT_ENDPOINTS.SYNC_SERVERS, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
    });
    return response.json();
  },

  async getOneConnectServers() {
    const response = await fetch(ONECONNECT_ENDPOINTS.GET_SERVERS, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
    });
    return response.json();
  },

  // Ad Management API calls
  async saveAdConfig(config: AdNetworkConfiguration) {
    const response = await fetch(AD_ENDPOINTS.SAVE_AD_CONFIG, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
      body: JSON.stringify(config),
    });
    return response.json();
  },

  async getAdConfig() {
    const response = await fetch(AD_ENDPOINTS.GET_AD_CONFIG, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
    });
    return response.json();
  },

  async getAdStatistics() {
    const response = await fetch(AD_ENDPOINTS.GET_AD_STATISTICS, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
    });
    return response.json();
  },

  // Add points
  async addPoints(userId: string, points: number, reason: string) {
    const response = await fetch(REWARDS_ENDPOINTS.ADD_POINTS, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
      body: JSON.stringify({ user_id: userId, points, reason }),
    });
    return response.json();
  },

  // Get app configuration
  async getAppConfig() {
    const response = await fetch(SETTINGS_ENDPOINTS.GET_APP_CONFIG);
    return response.json();
  },

  // Record ad view
  async recordAdView(adType: string, adId: string, network: string) {
    const response = await fetch(AD_ENDPOINTS.RECORD_AD_VIEW, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
      body: JSON.stringify({ ad_type: adType, ad_id: adId, network }),
    });
    return response.json();
  },

  // Notification API calls
  async sendBroadcastNotification(title: string, message: string) {
    const response = await fetch(NOTIFICATION_ENDPOINTS.SEND_BROADCAST, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
      body: JSON.stringify({ title, message }),
    });
    return response.json();
  },

  async sendIndividualNotification(title: string, message: string, userEmail: string) {
    const response = await fetch(NOTIFICATION_ENDPOINTS.SEND_INDIVIDUAL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
      body: JSON.stringify({ title, message, user_email: userEmail }),
    });
    return response.json();
  },

  async getNotificationHistory() {
    const response = await fetch(NOTIFICATION_ENDPOINTS.GET_NOTIFICATION_HISTORY, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
    });
    return response.json();
  },

  async getUserNotifications() {
    const response = await fetch(NOTIFICATION_ENDPOINTS.GET_USER_NOTIFICATIONS, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
    });
    return response.json();
  },

  async markNotificationAsRead(notificationId: string) {
    const response = await fetch(NOTIFICATION_ENDPOINTS.MARK_AS_READ.replace('{id}', notificationId), {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
    });
    return response.json();
  },

  // Support API calls
  async createSupportTicket(subject: string, message: string, priority: string = 'medium') {
    const response = await fetch(SUPPORT_ENDPOINTS.CREATE_TICKET, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
      body: JSON.stringify({ subject, message, priority }),
    });
    return response.json();
  },

  async getAllSupportTickets() {
    const response = await fetch(SUPPORT_ENDPOINTS.GET_ALL_TICKETS, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
    });
    return response.json();
  },

  async updateTicketStatus(ticketId: string, status: string) {
    const response = await fetch(SUPPORT_ENDPOINTS.UPDATE_TICKET_STATUS.replace('{id}', ticketId), {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
      body: JSON.stringify({ status }),
    });
    return response.json();
  },

  async replyToTicket(ticketId: string, message: string) {
    const response = await fetch(SUPPORT_ENDPOINTS.REPLY_TO_TICKET.replace('{id}', ticketId), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
      body: JSON.stringify({ message }),
    });
    return response.json();
  },

  // Blog API calls
  async getBlogPosts(page: number = 1, limit: number = 10) {
    const response = await fetch(`${BLOG_ENDPOINTS.GET_POSTS}?page=${page}&limit=${limit}`);
    return response.json();
  },

  async getBlogPost(postId: string) {
    const response = await fetch(BLOG_ENDPOINTS.GET_POST.replace('{id}', postId));
    return response.json();
  },

  async createBlogPost(postData: BlogPostData) {
    const response = await fetch(BLOG_ENDPOINTS.CREATE_POST, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
      body: JSON.stringify(postData),
    });
    return response.json();
  },

  async updateBlogPost(postId: string, postData: BlogPostData) {
    const response = await fetch(BLOG_ENDPOINTS.UPDATE_POST.replace('{id}', postId), {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
      body: JSON.stringify(postData),
    });
    return response.json();
  },

  async deleteBlogPost(postId: string) {
    const response = await fetch(BLOG_ENDPOINTS.DELETE_POST.replace('{id}', postId), {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
    });
    return response.json();
  },

  async incrementPostViews(postId: string) {
    const response = await fetch(BLOG_ENDPOINTS.INCREMENT_VIEWS.replace('{id}', postId), {
      method: 'POST',
    });
    return response.json();
  },

  // Leaderboard API calls
  async getLeaderboard(period: string = 'all-time', category: string = 'points') {
    const response = await fetch(`${LEADERBOARD_ENDPOINTS.GET_LEADERBOARD}?period=${period}&category=${category}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
    });
    return response.json();
  },

  async getUserRank(userId: string) {
    const response = await fetch(LEADERBOARD_ENDPOINTS.GET_USER_RANK.replace('{id}', userId),  {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
    });
    return response.json();
  },

  async getTopUsers(limit: number = 10) {
    const response = await fetch(LEADERBOARD_ENDPOINTS.GET_TOP_USERS.replace('{limit}', limit.toString()), {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
    });
    return response.json();
  },

  async getAchievements() {
    const response = await fetch(LEADERBOARD_ENDPOINTS.GET_ACHIEVEMENTS, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
    });
    return response.json();
  },

  async getUserAchievements(userId: string) {
    const response = await fetch(LEADERBOARD_ENDPOINTS.GET_USER_ACHIEVEMENTS.replace('{id}', userId), {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
    });
    return response.json();
  },

  async rewardTopUsers(limit: number = 10, points: number = 1000) {
    const response = await fetch(LEADERBOARD_ENDPOINTS.REWARD_TOP_USERS, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
      body: JSON.stringify({ limit, points }),
    });
    return response.json();
  },

  async resetLeaderboard(period: string) {
    const response = await fetch(LEADERBOARD_ENDPOINTS.RESET_LEADERBOARD, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`,
      },
      body: JSON.stringify({ period }),
    });
    return response.json();
  },
};

// Sample API Response Types (for TypeScript)
export interface Server {
  id: number;
  name: string;
  country: string;
  city?: string;
  ip: string;
  port: string;
  protocol: string;
  type: 'free' | 'premium';
  status: 'online' | 'offline' | 'maintenance';
  load: number;
  users: number;
  config_file?: string;
  provider?: 'manual' | 'oneconnect';
}

export interface OneConnectServer extends Server {
  provider: 'oneconnect';
  oneconnect_id: string;
  last_sync: string;
}

export interface OneConnectConfig {
  api_key: string;
  api_url: string;
  username: string;
  password?: string;
  last_sync?: string;
  status: 'connected' | 'disconnected' | 'error';
}

export interface User {
  id: number;
  name: string;
  email: string;
  phone?: string;
  plan: 'free' | 'premium';
  points: number;
  status: 'active' | 'blocked';
  subscription_expires?: string;
  created_at: string;
  last_seen: string;
}

export interface RewardEvent {
  id: number;
  user_id: number;
  event_type: string;
  points: number;
  description: string;
  created_at: string;
}

export interface SubscriptionPlan {
  id: number;
  name: string;
  duration: string;
  price: number;
  currency: string;
  features: string[];
  active: boolean;
}

// Enhanced Ad Network Configuration Types
export type AdNetworkType = 'admob' | 'facebook' | 'applovin' | 'unity' | 'adcolony' | 'startapp';

export interface AdNetworkConfig {
  enabled: boolean;
  app_id?: string;
  banner_id?: string;
  interstitial_id?: string;
  rewarded_id?: string;
  native_id?: string;
  app_open_id?: string; // Added App Open Ad support
  api_key?: string;
  placement_id?: string;
}

export interface AdNetworkConfiguration {
  admob: AdNetworkConfig;
  facebook: AdNetworkConfig;
  applovin: AdNetworkConfig;
  unity: AdNetworkConfig;
  adcolony: AdNetworkConfig;
  startapp: AdNetworkConfig;
  primary_rewarded_network: AdNetworkType;
  primary_interstitial_network: AdNetworkType;
  primary_banner_network: AdNetworkType;
  primary_app_open_network: AdNetworkType; // Added App Open primary network
  
  // Feature-specific ad network controls
  lucky_wheel_network: AdNetworkType;
  tic_tac_toe_network: AdNetworkType;
  scratch_card_network: AdNetworkType;
  premium_server_unlock_network: AdNetworkType;
  
  ad_frequency: number;
  reward_points: number;
}

export interface AppConfig {
  app_name: string;
  app_version: string;
  force_update: boolean;
  maintenance_mode: boolean;
  ad_config: AdNetworkConfiguration;
  features: {
    free_server_limit: number;
    auto_connect: boolean;
    kill_switch: boolean;
  };
}

// Add new interface for OneConnect configuration
export interface OneConnectConfiguration {
  id?: number;
  api_key: string;
  api_url: string;
  enabled: boolean;
  is_connected: boolean;
  last_sync?: string;
  created_at?: string;
  updated_at?: string;
}

export interface OneConnectServiceStatus {
  enabled: boolean;
  connected: boolean;
  server_count: number;
  last_sync: string | null;
  api_status: 'active' | 'inactive' | 'error';
}

export interface AdStatistics {
  network: AdNetworkType;
  impressions: number;
  clicks: number;
  revenue: number;
  fill_rate: number;
  ecpm: number;
}

// Add new interface types for the new features
export interface NotificationData {
  title: string;
  message: string;
  type: 'broadcast' | 'individual';
  recipient?: string;
}

export interface SupportTicketData {
  subject: string;
  message: string;
  priority: 'low' | 'medium' | 'high' | 'urgent';
}

export interface BlogPostData {
  title: string;
  content: string;
  excerpt: string;
  category: string;
  tags: string[];
  status: 'draft' | 'published' | 'archived';
}

export interface LeaderboardData {
  period: 'daily' | 'weekly' | 'monthly' | 'all-time';
  category: 'points' | 'referrals' | 'activity';
  limit?: number;
}

export interface Achievement {
  id: string;
  name: string;
  description: string;
  icon: string;
  category: 'points' | 'referrals' | 'activity' | 'premium';
  pointsRequired?: number;
}
