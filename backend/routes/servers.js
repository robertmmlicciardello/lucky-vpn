
const express = require('express');
const { Server } = require('../models');
const auth = require('../middleware/auth');
const adminAuth = require('../middleware/adminAuth');
const router = express.Router();

// Get all servers
router.get('/', auth, async (req, res) => {
  try {
    const { type } = req.query;
    let whereClause = { status: 'online' };
    
    if (type === 'free') {
      whereClause.type = 'free';
    } else if (type === 'premium') {
      whereClause.type = 'premium';
    }

    const servers = await Server.findAll({
      where: whereClause,
      attributes: ['id', 'name', 'country', 'city', 'ip', 'port', 'protocol', 'type', 'status', 'load', 'users'],
      order: [['load', 'ASC']]
    });

    res.json({
      success: true,
      data: servers
    });

  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Get free servers
router.get('/free', auth, async (req, res) => {
  try {
    const servers = await Server.findAll({
      where: { 
        status: 'online',
        type: 'free'
      },
      attributes: ['id', 'name', 'country', 'city', 'ip', 'port', 'protocol', 'type', 'load', 'users'],
      order: [['load', 'ASC']]
    });

    res.json({
      success: true,
      data: servers
    });

  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Get premium servers
router.get('/premium', auth, async (req, res) => {
  try {
    const servers = await Server.findAll({
      where: { 
        status: 'online',
        type: 'premium'
      },
      attributes: ['id', 'name', 'country', 'city', 'ip', 'port', 'protocol', 'type', 'load', 'users'],
      order: [['load', 'ASC']]
    });

    res.json({
      success: true,
      data: servers
    });

  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Admin routes - Add server
router.post('/', adminAuth, async (req, res) => {
  try {
    const { name, country, city, ip, port, protocol, type, config_file } = req.body;

    const server = await Server.create({
      name,
      country,
      city,
      ip,
      port: port || '1194',
      protocol: protocol || 'OpenVPN',
      type: type || 'free',
      config_file,
      provider: 'manual'
    });

    res.status(201).json({
      success: true,
      message: 'Server added successfully',
      data: server
    });

  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Admin routes - Update server
router.put('/:id', adminAuth, async (req, res) => {
  try {
    const { id } = req.params;
    const updateData = req.body;

    const server = await Server.findByPk(id);
    if (!server) {
      return res.status(404).json({
        success: false,
        message: 'Server not found'
      });
    }

    await server.update(updateData);

    res.json({
      success: true,
      message: 'Server updated successfully',
      data: server
    });

  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Admin routes - Delete server
router.delete('/:id', adminAuth, async (req, res) => {
  try {
    const { id } = req.params;

    const server = await Server.findByPk(id);
    if (!server) {
      return res.status(404).json({
        success: false,
        message: 'Server not found'
      });
    }

    await server.destroy();

    res.json({
      success: true,
      message: 'Server deleted successfully'
    });

  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Get server configuration file
router.get('/:id/config', auth, async (req, res) => {
  try {
    const { id } = req.params;
    
    const server = await Server.findByPk(id);
    if (!server) {
      return res.status(404).json({
        success: false,
        message: 'Server not found'
      });
    }

    res.json({
      success: true,
      data: {
        config_file: server.config_file
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
