
const express = require('express');
const axios = require('axios');
const { OneConnectConfig, Server } = require('../models');
const adminAuth = require('../middleware/adminAuth');
const router = express.Router();

// Get OneConnect configuration
router.get('/config', adminAuth, async (req, res) => {
  try {
    const config = await OneConnectConfig.findOne();
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

// Update OneConnect configuration
router.put('/config', adminAuth, async (req, res) => {
  try {
    const { api_url, api_key, auto_sync, sync_interval } = req.body;

    let config = await OneConnectConfig.findOne();
    if (!config) {
      config = await OneConnectConfig.create({
        api_url,
        api_key,
        auto_sync,
        sync_interval
      });
    } else {
      await config.update({
        api_url,
        api_key,
        auto_sync,
        sync_interval
      });
    }

    res.json({
      success: true,
      message: 'Configuration updated successfully'
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Sync servers from OneConnect
router.post('/sync', adminAuth, async (req, res) => {
  try {
    const config = await OneConnectConfig.findOne();
    if (!config) {
      return res.status(400).json({
        success: false,
        message: 'OneConnect not configured'
      });
    }

    const response = await axios.get(`${config.api_url}/servers`, {
      headers: {
        'Authorization': `Bearer ${config.api_key}`
      }
    });

    const servers = response.data.servers;
    let syncedCount = 0;

    for (const serverData of servers) {
      const [server, created] = await Server.findOrCreate({
        where: { oneconnect_id: serverData.id },
        defaults: {
          name: serverData.name,
          country: serverData.country,
          city: serverData.city,
          ip: serverData.ip,
          port: serverData.port,
          protocol: serverData.protocol,
          type: serverData.type,
          provider: 'oneconnect',
          oneconnect_id: serverData.id,
          config_file: serverData.config
        }
      });

      if (!created) {
        await server.update({
          name: serverData.name,
          country: serverData.country,
          city: serverData.city,
          ip: serverData.ip,
          port: serverData.port,
          protocol: serverData.protocol,
          type: serverData.type,
          config_file: serverData.config,
          last_sync: new Date()
        });
      }
      syncedCount++;
    }

    res.json({
      success: true,
      message: `Synced ${syncedCount} servers successfully`
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Sync failed'
    });
  }
});

module.exports = router;
