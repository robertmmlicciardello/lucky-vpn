
const express = require('express');
const cors = require('cors');
require('dotenv').config();

// Import security and middleware
const { authLimiter, generalLimiter, paymentLimiter, securityConfig } = require('./config/security');
const { globalErrorHandler, notFound, asyncHandler } = require('./middleware/errorHandler');
const { compressionMiddleware, responseTimeMiddleware } = require('./middleware/performance');
const { specs, swaggerUi } = require('./config/swagger');

// Import routes
const authRoutes = require('./routes/auth');
const userRoutes = require('./routes/users');
const serverRoutes = require('./routes/servers');
const paymentRoutes = require('./routes/payments');
const rewardRoutes = require('./routes/rewards');
const adminRoutes = require('./routes/admin');
const oneConnectRoutes = require('./routes/oneconnect');
const adRoutes = require('./routes/ads');
const notificationRoutes = require('./routes/notifications');
const supportRoutes = require('./routes/support');
const blogRoutes = require('./routes/blog');
const leaderboardRoutes = require('./routes/leaderboard');

const db = require('./models');
const logger = require('./utils/logger');

const app = express();
const PORT = process.env.PORT || 3000;

// Validate required environment variables
const requiredEnvVars = ['JWT_SECRET', 'DB_HOST', 'DB_USER', 'DB_NAME'];
for (const envVar of requiredEnvVars) {
  if (!process.env[envVar]) {
    logger.error(`Missing required environment variable: ${envVar}`);
    process.exit(1);
  }
}

// Trust proxy for accurate IP addresses
app.set('trust proxy', 1);

// Performance middleware
app.use(compressionMiddleware);
app.use(responseTimeMiddleware);

// Security middleware
app.use(securityConfig.helmet);
app.use(cors(securityConfig.cors));

// Rate limiting
app.use('/api/v1/auth/login', authLimiter);
app.use('/api/v1/auth/register', authLimiter);
app.use('/api/v1/auth/admin/login', authLimiter);
app.use('/api/v1/subscription/submit', paymentLimiter);
app.use('/api/v1', generalLimiter);

// Body parsing middleware with size limits
app.use(express.json({ 
  limit: process.env.UPLOAD_MAX_SIZE || '10mb',
  verify: (req, res, buf) => {
    req.rawBody = buf;
  }
}));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// Additional security headers middleware
app.use((req, res, next) => {
  res.setHeader('X-Content-Type-Options', 'nosniff');
  res.setHeader('X-Frame-Options', 'DENY');
  res.setHeader('X-XSS-Protection', '1; mode=block');
  res.setHeader('Referrer-Policy', 'strict-origin-when-cross-origin');
  res.setHeader('Permissions-Policy', 'geolocation=(), microphone=(), camera=()');
  next();
});

// API Documentation
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(specs, {
  explorer: true,
  customCss: '.swagger-ui .topbar { display: none }',
  customSiteTitle: 'Monetize VPN API Documentation'
}));

// API routes
app.use('/api/v1/auth', authRoutes);
app.use('/api/v1/users', userRoutes);
app.use('/api/v1/servers', serverRoutes);
app.use('/api/v1/subscription', paymentRoutes);
app.use('/api/v1/rewards', rewardRoutes);
app.use('/api/v1/admin', adminRoutes);
app.use('/api/v1/oneconnect', oneConnectRoutes);
app.use('/api/v1/ads', adRoutes);
app.use('/api/v1/notifications', notificationRoutes);
app.use('/api/v1/support', supportRoutes);
app.use('/api/v1/blog', blogRoutes);
app.use('/api/v1/leaderboard', leaderboardRoutes);

// Health check endpoint with detailed information
app.get('/health', asyncHandler(async (req, res) => {
  const healthCheck = {
    status: 'OK',
    timestamp: new Date().toISOString(),
    version: process.env.npm_package_version || '1.0.0',
    environment: process.env.NODE_ENV || 'development',
    uptime: process.uptime(),
    memory: process.memoryUsage(),
    database: 'connected' // You can add actual DB health check here
  };
  
  res.json(healthCheck);
}));

// API status endpoint
app.get('/api/v1/status', (req, res) => {
  res.json({
    success: true,
    message: 'Monetize VPN API is running',
    version: '1.0.0',
    documentation: '/api-docs'
  });
});

// 404 handler
app.use('*', notFound);

// Global error handling middleware
app.use(globalErrorHandler);

// Graceful shutdown handling
const gracefulShutdown = (signal) => {
  logger.info(`${signal} received, shutting down gracefully`);
  
  server.close((err) => {
    if (err) {
      logger.error('Error during server shutdown:', err);
      process.exit(1);
    }
    
    // Close database connections
    db.sequelize.close().then(() => {
      logger.info('Database connections closed');
      logger.info('Process terminated gracefully');
      process.exit(0);
    }).catch((dbErr) => {
      logger.error('Error closing database connections:', dbErr);
      process.exit(1);
    });
  });
  
  // Force close after 10 seconds
  setTimeout(() => {
    logger.error('Could not close connections in time, forcefully shutting down');
    process.exit(1);
  }, 10000);
};

process.on('SIGTERM', () => gracefulShutdown('SIGTERM'));
process.on('SIGINT', () => gracefulShutdown('SIGINT'));

// Handle uncaught exceptions
process.on('uncaughtException', (err) => {
  logger.error('UNCAUGHT EXCEPTION! 💥 Shutting down...', err);
  process.exit(1);
});

// Handle unhandled promise rejections
process.on('unhandledRejection', (err) => {
  logger.error('UNHANDLED REJECTION! 💥 Shutting down...', err);
  server.close(() => {
    process.exit(1);
  });
});

// Database sync and server start
let server;
db.sequelize.sync({ force: false }).then(() => {
  server = app.listen(PORT, () => {
    logger.info(`🚀 Monetize VPN Server started successfully`);
    logger.info(`📊 Server running on port ${PORT}`);
    logger.info(`🌍 Environment: ${process.env.NODE_ENV || 'development'}`);
    logger.info(`🔗 Frontend URL: ${process.env.FRONTEND_URL}`);
    logger.info(`📚 API Documentation: http://localhost:${PORT}/api-docs`);
    logger.info(`💚 Health Check: http://localhost:${PORT}/health`);
  });
}).catch(err => {
  logger.error('Unable to connect to the database:', err);
  process.exit(1);
});

module.exports = app;
