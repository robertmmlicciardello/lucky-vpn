
const express = require('express');
const { Reward, User } = require('../models');
const auth = require('../middleware/auth');
const adminAuth = require('../middleware/adminAuth');
const router = express.Router();

// Get user rewards
router.get('/', auth, async (req, res) => {
  try {
    const rewards = await Reward.findAll({
      where: { user_id: req.user.id },
      order: [['createdAt', 'DESC']]
    });

    res.json({
      success: true,
      data: rewards
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Claim daily reward
router.post('/daily', auth, async (req, res) => {
  try {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const existingReward = await Reward.findOne({
      where: {
        user_id: req.user.id,
        type: 'daily',
        createdAt: {
          [require('sequelize').Op.gte]: today
        }
      }
    });

    if (existingReward) {
      return res.status(400).json({
        success: false,
        message: 'Daily reward already claimed'
      });
    }

    const points = 100;
    await Reward.create({
      user_id: req.user.id,
      type: 'daily',
      points,
      description: 'Daily login reward'
    });

    await req.user.increment('points', { by: points });

    res.json({
      success: true,
      message: 'Daily reward claimed',
      points
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Watch ad reward
router.post('/watch-ad', auth, async (req, res) => {
  try {
    const points = 50;
    await Reward.create({
      user_id: req.user.id,
      type: 'ad',
      points,
      description: 'Watched advertisement'
    });

    await req.user.increment('points', { by: points });

    res.json({
      success: true,
      message: 'Ad reward claimed',
      points
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Admin - Get all rewards
router.get('/all', adminAuth, async (req, res) => {
  try {
    const rewards = await Reward.findAll({
      include: [{
        model: User,
        attributes: ['name', 'email']
      }],
      order: [['createdAt', 'DESC']]
    });

    res.json({
      success: true,
      data: rewards
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Lucky Wheel spin — random 10-1000 pts, once per hour per user
router.post('/wheel', auth, async (req, res) => {
  try {
    const { Op } = require('sequelize');
    const oneHourAgo = new Date(Date.now() - 60 * 60 * 1000);
    const recent = await Reward.findOne({
      where: { user_id: req.user.id, type: 'wheel', createdAt: { [Op.gte]: oneHourAgo } }
    });
    if (recent) {
      return res.status(400).json({ success: false, message: 'Wheel spin available once per hour' });
    }
    // Weighted random: mostly small wins, rarely big
    const roll = Math.random() * 100;
    let points;
    if (roll < 40) points = 10 + Math.floor(Math.random() * 40);        // 10-49
    else if (roll < 70) points = 50 + Math.floor(Math.random() * 50);   // 50-99
    else if (roll < 90) points = 100 + Math.floor(Math.random() * 100); // 100-199
    else if (roll < 98) points = 200 + Math.floor(Math.random() * 300); // 200-499
    else points = 500 + Math.floor(Math.random() * 501);                // 500-1000

    await Reward.create({
      user_id: req.user.id, type: 'wheel', points,
      description: `Lucky wheel spin: +${points} pts`
    });
    await req.user.increment('points', { by: points });

    res.json({ success: true, message: 'Wheel spin complete', points });
  } catch (error) {
    console.error(error);
    res.status(500).json({ success: false, message: 'Server error' });
  }
});

// Share app — 200 pts, once per day
router.post('/share', auth, async (req, res) => {
  try {
    const { Op } = require('sequelize');
    const today = new Date(); today.setHours(0, 0, 0, 0);
    const existing = await Reward.findOne({
      where: { user_id: req.user.id, type: 'share', createdAt: { [Op.gte]: today } }
    });
    if (existing) {
      return res.status(400).json({ success: false, message: 'Share reward already claimed today' });
    }
    const points = 200;
    await Reward.create({
      user_id: req.user.id, type: 'share', points,
      description: 'Shared the app'
    });
    await req.user.increment('points', { by: points });
    res.json({ success: true, message: 'Share reward claimed', points });
  } catch (error) {
    console.error(error);
    res.status(500).json({ success: false, message: 'Server error' });
  }
});

// Rate app — 300 pts, once ever
router.post('/rate', auth, async (req, res) => {
  try {
    const existing = await Reward.findOne({
      where: { user_id: req.user.id, type: 'rate' }
    });
    if (existing) {
      return res.status(400).json({ success: false, message: 'Rate reward already claimed' });
    }
    const points = 300;
    await Reward.create({
      user_id: req.user.id, type: 'rate', points,
      description: 'Rated the app'
    });
    await req.user.increment('points', { by: points });
    res.json({ success: true, message: 'Rate reward claimed', points });
  } catch (error) {
    console.error(error);
    res.status(500).json({ success: false, message: 'Server error' });
  }
});

// Redeem points — currently: 'premium_7d' costs 1000 pts → 7 days premium
router.post('/redeem', auth, async (req, res) => {
  try {
    const { reward } = req.body;
    const catalog = { premium_7d: { cost: 1000, days: 7, label: '7 days Premium' } };
    const item = catalog[reward];
    if (!item) {
      return res.status(400).json({ success: false, message: 'Unknown reward' });
    }
    const user = await User.findByPk(req.user.id);
    if (user.points < item.cost) {
      return res.status(400).json({
        success: false,
        message: `Not enough points (need ${item.cost}, have ${user.points})`
      });
    }
    await Reward.create({
      user_id: user.id, type: 'redeem', points: -item.cost,
      description: `Redeemed: ${item.label}`
    });
    await user.decrement('points', { by: item.cost });

    // Grant premium: extend from now or from existing expiry, whichever later
    const base = user.subscription_expires && new Date(user.subscription_expires) > new Date()
      ? new Date(user.subscription_expires) : new Date();
    base.setDate(base.getDate() + item.days);
    await user.update({ plan: 'premium', subscription_expires: base });

    res.json({
      success: true, message: `${item.label} activated`,
      points_spent: item.cost,
      premium_expires: base.toISOString()
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ success: false, message: 'Server error' });
  }
});

module.exports = router;
