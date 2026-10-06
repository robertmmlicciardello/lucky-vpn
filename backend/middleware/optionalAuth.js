const jwt = require('jsonwebtoken');
const { User } = require('../models');

/**
 * Like auth.js, but guests are allowed through with req.user = null
 * instead of a 401. Use for endpoints that are public for free content
 * (e.g. free server list) but gated for premium content.
 */
module.exports = async (req, res, next) => {
  const token = req.header('Authorization')?.replace('Bearer ', '');

  if (!token || token === 'null' || token === 'undefined') {
    req.user = null;
    return next();
  }

  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    const user = await User.findByPk(decoded.id);

    if (!user || user.status === 'blocked') {
      req.user = null;
      return next();
    }

    req.user = user;
    next();
  } catch (error) {
    req.user = null;
    next();
  }
};
