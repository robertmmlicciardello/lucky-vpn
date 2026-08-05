
const winston = require('winston');
const path = require('path');

// Create logs directory if it doesn't exist
const fs = require('fs');
const logsDir = 'logs';
if (!fs.existsSync(logsDir)) {
  fs.mkdirSync(logsDir);
}

// Custom format to filter sensitive information
const sensitiveDataFilter = winston.format((info) => {
  const sensitiveFields = ['password', 'token', 'jwt', 'secret', 'api_key', 'credit_card'];
  
  if (info.message && typeof info.message === 'object') {
    sensitiveFields.forEach(field => {
      if (info.message[field]) {
        info.message[field] = '[FILTERED]';
      }
    });
  }
  
  return info;
});

const logger = winston.createLogger({
  level: process.env.LOG_LEVEL || 'info',
  format: winston.format.combine(
    winston.format.timestamp({ format: 'YYYY-MM-DD HH:mm:ss' }),
    winston.format.errors({ stack: true }),
    sensitiveDataFilter(),
    winston.format.json()
  ),
  defaultMeta: { 
    service: 'monetize-vpn-api',
    version: process.env.npm_package_version || '1.0.0'
  },
  transports: [
    // Error logs
    new winston.transports.File({ 
      filename: path.join(logsDir, 'error.log'), 
      level: 'error',
      maxsize: 5242880, // 5MB
      maxFiles: 5
    }),
    // Combined logs
    new winston.transports.File({ 
      filename: path.join(logsDir, 'combined.log'),
      maxsize: 5242880, // 5MB
      maxFiles: 5
    }),
    // Access logs
    new winston.transports.File({ 
      filename: path.join(logsDir, 'access.log'),
      level: 'http',
      maxsize: 10485760, // 10MB
      maxFiles: 3
    })
  ],
  // Handle uncaught exceptions
  exceptionHandlers: [
    new winston.transports.File({ filename: path.join(logsDir, 'exceptions.log') })
  ],
  // Handle unhandled promise rejections
  rejectionHandlers: [
    new winston.transports.File({ filename: path.join(logsDir, 'rejections.log') })
  ]
});

// Console logging for development
if (process.env.NODE_ENV !== 'production') {
  logger.add(new winston.transports.Console({
    format: winston.format.combine(
      winston.format.colorize(),
      winston.format.simple()
    )
  }));
}

// Add request logging method
logger.logRequest = (req, res, responseTime) => {
  const logData = {
    method: req.method,
    url: req.url,
    status: res.statusCode,
    responseTime: `${responseTime}ms`,
    userAgent: req.get('User-Agent'),
    ip: req.ip,
    userId: req.user ? req.user.id : null
  };
  
  logger.log('http', 'Request processed', logData);
};

module.exports = logger;
