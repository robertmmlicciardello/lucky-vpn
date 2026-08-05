
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

module.exports = router;
