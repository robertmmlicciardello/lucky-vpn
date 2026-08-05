
-- Lucky VPN Master Database Setup
-- Run this script to create the database and tables

CREATE DATABASE IF NOT EXISTS lucky_vpn_master;
USE lucky_vpn_master;

-- Users table
CREATE TABLE Users (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  phone VARCHAR(20),
  plan ENUM('free', 'premium') DEFAULT 'free',
  status ENUM('active', 'blocked') DEFAULT 'active',
  points INT DEFAULT 0,
  subscription_expires DATETIME NULL,
  referral_code VARCHAR(10) UNIQUE,
  referred_by INT,
  last_seen DATETIME,
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (referred_by) REFERENCES Users(id)
);

-- Admins table
CREATE TABLE Admins (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  role ENUM('admin', 'super_admin') DEFAULT 'admin',
  status ENUM('active', 'inactive') DEFAULT 'active',
  last_login DATETIME,
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Servers table
CREATE TABLE Servers (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  country VARCHAR(100) NOT NULL,
  city VARCHAR(100),
  ip VARCHAR(45) NOT NULL,
  port VARCHAR(10) DEFAULT '1194',
  protocol ENUM('OpenVPN', 'IKEv2', 'WireGuard') DEFAULT 'OpenVPN',
  type ENUM('free', 'premium') DEFAULT 'free',
  status ENUM('online', 'offline', 'maintenance') DEFAULT 'online',
  load_percentage INT DEFAULT 0,
  users_count INT DEFAULT 0,
  provider ENUM('manual', 'oneconnect') DEFAULT 'manual',
  oneconnect_id VARCHAR(255),
  config_file TEXT,
  last_sync DATETIME,
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Payments table
CREATE TABLE Payments (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  transaction_id VARCHAR(255) UNIQUE NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  payment_method ENUM('kpay', 'wave', 'aya', 'cb', 'bank') NOT NULL,
  gateway_account VARCHAR(255),
  plan_type ENUM('monthly', 'quarterly', 'yearly') NOT NULL,
  duration INT NOT NULL,
  status ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
  screenshot TEXT,
  admin_notes TEXT,
  approved_at DATETIME,
  approved_by INT,
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES Users(id),
  FOREIGN KEY (approved_by) REFERENCES Admins(id)
);

-- Rewards table
CREATE TABLE Rewards (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  type ENUM('daily', 'ad', 'referral', 'bonus') NOT NULL,
  points INT NOT NULL,
  description VARCHAR(255),
  claimed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES Users(id)
);

-- Notifications table
CREATE TABLE Notifications (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  title VARCHAR(255) NOT NULL,
  message TEXT NOT NULL,
  type ENUM('general', 'payment', 'server', 'promotion') DEFAULT 'general',
  is_read BOOLEAN DEFAULT FALSE,
  action_url VARCHAR(255),
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES Users(id)
);

-- Support Tickets table
CREATE TABLE SupportTickets (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  subject VARCHAR(255) NOT NULL,
  message TEXT NOT NULL,
  category ENUM('general', 'technical', 'billing', 'feature') DEFAULT 'general',
  status ENUM('open', 'in_progress', 'resolved', 'closed') DEFAULT 'open',
  admin_response TEXT,
  resolved_at DATETIME,
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES Users(id)
);

-- Blog Posts table
CREATE TABLE BlogPosts (
  id INT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  excerpt TEXT,
  featured_image VARCHAR(255),
  status ENUM('draft', 'published') DEFAULT 'draft',
  author_id INT NOT NULL,
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (author_id) REFERENCES Admins(id)
);

-- Ad Configuration table
CREATE TABLE AdConfigs (
  id INT PRIMARY KEY AUTO_INCREMENT,
  admob_app_id VARCHAR(255),
  admob_banner_id VARCHAR(255),
  admob_interstitial_id VARCHAR(255),
  admob_rewarded_id VARCHAR(255),
  facebook_app_id VARCHAR(255),
  facebook_banner_id VARCHAR(255),
  facebook_interstitial_id VARCHAR(255),
  facebook_rewarded_id VARCHAR(255),
  unity_game_id VARCHAR(255),
  unity_banner_id VARCHAR(255),
  unity_interstitial_id VARCHAR(255),
  unity_rewarded_id VARCHAR(255),
  primary_network ENUM('admob', 'facebook', 'unity') DEFAULT 'admob',
  show_ads BOOLEAN DEFAULT TRUE,
  ad_frequency INT DEFAULT 3,
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- OneConnect Configuration table
CREATE TABLE OneConnectConfigs (
  id INT PRIMARY KEY AUTO_INCREMENT,
  api_url VARCHAR(255) NOT NULL,
  api_key VARCHAR(255) NOT NULL,
  auto_sync BOOLEAN DEFAULT FALSE,
  sync_interval INT DEFAULT 60,
  last_sync DATETIME,
  status ENUM('active', 'inactive') DEFAULT 'active',
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- User Connections table
CREATE TABLE UserConnections (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  server_id INT NOT NULL,
  connected_at DATETIME NOT NULL,
  disconnected_at DATETIME,
  duration INT DEFAULT 0,
  bytes_sent BIGINT DEFAULT 0,
  bytes_received BIGINT DEFAULT 0,
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES Users(id),
  FOREIGN KEY (server_id) REFERENCES Servers(id)
);

-- Insert default admin user
INSERT INTO Admins (name, email, password, role) VALUES 
('Admin', 'admin@luckyvpn.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'super_admin');

-- Insert sample servers
INSERT INTO Servers (name, country, city, ip, port, protocol, type, status) VALUES 
('US East 1', 'United States', 'New York', '192.168.1.100', '1194', 'OpenVPN', 'free', 'online'),
('US West 1', 'United States', 'California', '192.168.1.101', '1194', 'OpenVPN', 'premium', 'online'),
('UK London 1', 'United Kingdom', 'London', '192.168.1.102', '1194', 'OpenVPN', 'premium', 'online'),
('Singapore 1', 'Singapore', 'Singapore', '192.168.1.103', '1194', 'OpenVPN', 'free', 'online'),
('Japan Tokyo 1', 'Japan', 'Tokyo', '192.168.1.104', '1194', 'OpenVPN', 'premium', 'online');

-- Insert default ad configuration
INSERT INTO AdConfigs (admob_app_id, primary_network, show_ads, ad_frequency) VALUES 
('ca-app-pub-3940256099942544~3347511713', 'admob', TRUE, 3);
