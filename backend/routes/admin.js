
const express = require('express');
const { User, Server, Payment, Reward, PaymentConfig } = require('../models');
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

// Trigger a VPNGate free-server sync (admin only).
// Pulls the public VPNGate list and upserts it as provider='vpngate' servers.
router.post('/vpngate-sync', adminAuth, async (req, res) => {
  try {
    const { fetchVpnGateCsv, parseVpnGateCsv, syncToDb } = require('../jobs/sync-vpngate');
    const db = require('../models');
    const servers = parseVpnGateCsv(await fetchVpnGateCsv());
    const result = await syncToDb(db, servers);
    res.json({ success: true, data: result });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'VPNGate sync failed: ' + error.message
    });
  }
});

// Get payment configuration (admin only). Creates the singleton row (id=1) if missing.
router.get('/payment-config', adminAuth, async (req, res) => {
  try {
    let config = await PaymentConfig.findByPk(1);
    if (!config) config = await PaymentConfig.create({ id: 1 });
    res.json({
      success: true,
      data: config
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Update payment configuration (admin only)
router.put('/payment-config', adminAuth, async (req, res) => {
  try {
    const fields = [
      'kpay_account_number', 'kpay_account_name', 'kpay_qr_url',
      'wave_account_number', 'wave_account_name', 'wave_qr_url',
      'monthly_price', 'yearly_price', 'currency',
      'kpay_enabled', 'wave_enabled', 'google_play_enabled'
    ];
    const body = {};
    fields.forEach(f => {
      if (req.body[f] !== undefined) body[f] = req.body[f];
    });

    let config = await PaymentConfig.findByPk(1);
    if (!config) {
      config = await PaymentConfig.create(Object.assign({ id: 1 }, body));
    } else {
      await config.update(body);
    }

    res.json({
      success: true,
      message: 'Payment config saved',
      data: config
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
