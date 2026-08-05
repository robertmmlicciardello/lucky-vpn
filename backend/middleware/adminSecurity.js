
const jwt = require('jsonwebtoken');
const crypto = require('crypto');

// Enhanced admin authentication middleware
const enhancedAdminAuth = async (req, res, next) => {
  try {
    const token = req.header('Authorization')?.replace('Bearer ', '');
    
    if (!token) {
      return res.status(401).json({
        success: false,
        message: 'Access denied. No token provided.'
      });
    }

    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    
    // Verify admin role
    if (decoded.role !== 'admin') {
      return res.status(403).json({
        success: false,
        message: 'Access denied. Admin privileges required.'
      });
    }

    // Check token expiration (additional security)
    if (decoded.exp < Date.now() / 1000) {
      return res.status(401).json({
        success: false,
        message: 'Token expired. Please login again.'
      });
    }

    req.admin = decoded;
    next();
  } catch (error) {
    res.status(401).json({
      success: false,
      message: 'Invalid token.'
    });
  }
};

// Admin session tracking
const adminSessionTracker = (req, res, next) => {
  const adminId = req.admin?.id;
  const sessionInfo = {
    adminId,
    ip: req.ip,
    userAgent: req.get('User-Agent'),
    timestamp: new Date(),
    action: `${req.method} ${req.path}`
  };
  
  // Log admin activities (implement with proper logging service)
  console.log('Admin Activity:', sessionInfo);
  
  next();
};

// Two-factor authentication preparation
const prepareTwoFactorAuth = (adminData) => {
  const secret = crypto.randomBytes(32).toString('hex');
  
  return {
    secret,
    qrCode: `otpauth://totp/LuckyVPN:${adminData.email}?secret=${secret}&issuer=LuckyVPN`,
    backupCodes: Array.from({ length: 10 }, () => 
      crypto.randomBytes(4).toString('hex').toUpperCase()
    )
  };
};

module.exports = {
  enhancedAdminAuth,
  adminSessionTracker,
  prepareTwoFactorAuth
};
