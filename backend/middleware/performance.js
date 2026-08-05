
const compression = require('compression');
const logger = require('../utils/logger');

// Compression middleware with custom filter
const compressionMiddleware = compression({
  filter: (req, res) => {
    // Don't compress responses if the request includes a cache-control: no-transform directive
    if (req.headers['cache-control'] && req.headers['cache-control'].includes('no-transform')) {
      return false;
    }
    
    // Use compression for all other responses
    return compression.filter(req, res);
  },
  level: 6, // Compression level (1-9, 6 is good balance)
  threshold: 1024 // Only compress responses larger than 1KB
});

// Response time tracking middleware
const responseTimeMiddleware = (req, res, next) => {
  const startTime = Date.now();
  
  res.on('finish', () => {
    const responseTime = Date.now() - startTime;
    
    // Log slow requests (> 1 second)
    if (responseTime > 1000) {
      logger.warn('Slow request detected', {
        method: req.method,
        url: req.url,
        responseTime: `${responseTime}ms`,
        userAgent: req.get('User-Agent'),
        ip: req.ip
      });
    }
    
    // Log all requests for monitoring
    logger.logRequest(req, res, responseTime);
  });
  
  next();
};

// Memory usage monitoring
const memoryMonitor = () => {
  const used = process.memoryUsage();
  const memoryInfo = {
    rss: Math.round(used.rss / 1024 / 1024 * 100) / 100,
    heapTotal: Math.round(used.heapTotal / 1024 / 1024 * 100) / 100,
    heapUsed: Math.round(used.heapUsed / 1024 / 1024 * 100) / 100,
    external: Math.round(used.external / 1024 / 1024 * 100) / 100
  };
  
  // Log memory usage every 5 minutes
  logger.info('Memory usage', memoryInfo);
  
  // Warn if memory usage is high
  if (memoryInfo.heapUsed > 500) { // 500MB threshold
    logger.warn('High memory usage detected', memoryInfo);
  }
};

// Start memory monitoring
setInterval(memoryMonitor, 5 * 60 * 1000); // Every 5 minutes

module.exports = {
  compressionMiddleware,
  responseTimeMiddleware
};
