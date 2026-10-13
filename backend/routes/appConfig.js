const express = require('express');
const { AppConfig } = require('../models');
const router = express.Router();

// Public app configuration (no auth).
// Only exposes fields the client needs: maintenance state, version info,
// branding, support links and the free-server limit.
router.get('/', async (req, res) => {
  try {
    let config = await AppConfig.findByPk(1);
    if (!config) config = await AppConfig.create({ id: 1 });
    const c = config.toJSON();

    res.json({
      success: true,
      data: {
        maintenance_mode: c.maintenance_mode,
        maintenance_message: c.maintenance_message,
        min_app_version: c.min_app_version,
        latest_app_version: c.latest_app_version,
        update_url: c.update_url,
        app_name: c.app_name,
        support_email: c.support_email,
        privacy_policy_url: c.privacy_policy_url,
        terms_of_service_url: c.terms_of_service_url,
        support_url: c.support_url,
        banner_image_url: c.banner_image_url,
        free_server_limit: c.free_server_limit
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
