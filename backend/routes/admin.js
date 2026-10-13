
const express = require('express');
const { User, Server, Payment, Reward, PaymentConfig, RewardConfig, AppConfig } = require('../models');
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

// Get reward configuration (admin only). Creates the singleton row (id=1) if missing.
router.get('/reward-config', adminAuth, async (req, res) => {
  try {
    let config = await RewardConfig.findByPk(1);
    if (!config) config = await RewardConfig.create({ id: 1 });
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

// Update reward configuration (admin only)
router.put('/reward-config', adminAuth, async (req, res) => {
  try {
    const fields = [
      'daily_checkin_points', 'watch_ad_points',
      'wheel_min_points', 'wheel_max_points',
      'share_points', 'rate_points', 'invite_points',
      'redeem_points_required', 'redeem_premium_days'
    ];
    const body = {};
    fields.forEach(f => {
      if (req.body[f] !== undefined) {
        const n = parseInt(req.body[f], 10);
        if (!Number.isNaN(n) && n >= 0) body[f] = n;
      }
    });

    let config = await RewardConfig.findByPk(1);
    if (!config) {
      config = await RewardConfig.create(Object.assign({ id: 1 }, body));
    } else {
      await config.update(body);
    }

    res.json({
      success: true,
      message: 'Reward config saved',
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

// Reward stats (admin only): total points awarded + transaction count
router.get('/reward-stats', adminAuth, async (req, res) => {
  try {
    const { Op } = require('sequelize');
    const totalAwarded = await Reward.sum('points', { where: { points: { [Op.gt]: 0 } } });
    const count = await Reward.count();
    res.json({
      success: true,
      data: { totalAwarded: totalAwarded || 0, count }
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Get app configuration (admin only)
router.get('/app-config', adminAuth, async (req, res) => {
  try {
    let config = await AppConfig.findByPk(1);
    if (!config) config = await AppConfig.create({ id: 1 });
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

// Update app configuration (admin only)
router.put('/app-config', adminAuth, async (req, res) => {
  try {
    const boolFields = [
      'maintenance_mode',
      'push_notifications_enabled', 'update_notifications_enabled',
      'promo_notifications_enabled'
    ];
    const intFields = ['free_server_limit'];
    const strFields = [
      'maintenance_message', 'min_app_version', 'latest_app_version',
      'update_url', 'app_name', 'support_email',
      'privacy_policy_url', 'terms_of_service_url', 'support_url',
      'banner_image_url'
    ];
    const body = {};
    boolFields.forEach(f => {
      if (req.body[f] !== undefined) body[f] = !!req.body[f];
    });
    intFields.forEach(f => {
      if (req.body[f] !== undefined) {
        const n = parseInt(req.body[f], 10);
        if (!Number.isNaN(n) && n >= 0) body[f] = n;
      }
    });
    strFields.forEach(f => {
      if (req.body[f] !== undefined) body[f] = String(req.body[f]);
    });

    let config = await AppConfig.findByPk(1);
    if (!config) {
      config = await AppConfig.create(Object.assign({ id: 1 }, body));
    } else {
      await config.update(body);
    }

    res.json({
      success: true,
      message: 'App config saved',
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
