
const express = require('express');
const { AdConfig } = require('../models');
const auth = require('../middleware/auth');
const adminAuth = require('../middleware/adminAuth');
const router = express.Router();

// Get ad configuration for mobile app
router.get('/config', auth, async (req, res) => {
  try {
    const config = await AdConfig.findOne();
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

// Admin - Get ad configuration
router.get('/admin/config', adminAuth, async (req, res) => {
  try {
    const config = await AdConfig.findOne();
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

// Admin - Update ad configuration
router.put('/admin/config', adminAuth, async (req, res) => {
  try {
    const { 
      admob_app_id, 
      admob_banner_id, 
      admob_interstitial_id, 
      admob_rewarded_id,
      facebook_app_id,
      facebook_banner_id,
      facebook_interstitial_id,
      facebook_rewarded_id,
      unity_game_id,
      unity_banner_id,
      unity_interstitial_id,
      unity_rewarded_id,
      primary_network,
      show_ads,
      ad_frequency
    } = req.body;

    let config = await AdConfig.findOne();
    if (!config) {
      config = await AdConfig.create(req.body);
    } else {
      await config.update(req.body);
    }

    res.json({
      success: true,
      message: 'Ad configuration updated successfully'
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
