
const express = require('express');
const { User, Server, Payment, Reward } = require('../models');
const adminAuth = require('../middleware/adminAuth');
const router = express.Router();

// Dashboard stats
router.get('/stats', adminAuth, async (req, res) => {
  try {
    const totalUsers = await User.count();
    const premiumUsers = await User.count({ where: { plan: 'premium' } });
    const activeServers = await Server.count({ where: { status: 'online' } });
    const pendingPayments = await Payment.count({ where: { status: 'pending' } });
    const totalRevenue = await Payment.sum('amount', { where: { status: 'approved' } });

    res.json({
      success: true,
      data: {
        totalUsers,
        premiumUsers,
        activeServers,
        pendingPayments,
        totalRevenue: totalRevenue || 0
      }
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Recent activity
router.get('/activity', adminAuth, async (req, res) => {
  try {
    const recentUsers = await User.findAll({
      limit: 5,
      order: [['createdAt', 'DESC']],
      attributes: ['name', 'email', 'plan', 'createdAt']
    });

    const recentPayments = await Payment.findAll({
      limit: 5,
      order: [['createdAt', 'DESC']],
      include: [{
        model: User,
        attributes: ['name', 'email']
      }]
    });

    res.json({
      success: true,
      data: {
        recentUsers,
        recentPayments
      }
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

module.exports = router;
