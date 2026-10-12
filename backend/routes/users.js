
const express = require('express');
const { User, Payment, UserConnection } = require('../models');
const auth = require('../middleware/auth');
const adminAuth = require('../middleware/adminAuth');
const router = express.Router();

// Get user profile
router.get('/profile', auth, async (req, res) => {
  try {
    const user = await User.findByPk(req.user.id, {
      attributes: { exclude: ['password'] }
    });

    res.json({
      success: true,
      data: user
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Update user profile
router.put('/profile', auth, async (req, res) => {
  try {
    const { name, phone } = req.body;
    
    await req.user.update({ name, phone });

    res.json({
      success: true,
      message: 'Profile updated successfully'
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Admin - Get all users
router.get('/', adminAuth, async (req, res) => {
  try {
    const { page = 1, limit = 10, search } = req.query;
    let whereClause = {};

    if (search) {
      whereClause = {
        [require('sequelize').Op.or]: [
          { name: { [require('sequelize').Op.like]: `%${search}%` } },
          { email: { [require('sequelize').Op.like]: `%${search}%` } }
        ]
      };
    }

    const users = await User.findAndCountAll({
      where: whereClause,
      attributes: { exclude: ['password'] },
      limit: parseInt(limit),
      offset: (parseInt(page) - 1) * parseInt(limit),
      order: [['createdAt', 'DESC']]
    });

    res.json({
      success: true,
      data: users.rows,
      pagination: {
        total: users.count,
        page: parseInt(page),
        pages: Math.ceil(users.count / parseInt(limit))
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

// Admin - Block/Unblock user
router.put('/:id/status', adminAuth, async (req, res) => {
  try {
    const { id } = req.params;
    const { status } = req.body;

    const user = await User.findByPk(id);
    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'User not found'
      });
    }

    await user.update({ status });

    res.json({
      success: true,
      message: `User ${status} successfully`
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// ---- Connection tracking (UserConnection) ----
// Record a VPN session start. Body: { server_id }
router.post('/connections', auth, async (req, res) => {
  try {
    const { UserConnection } = require('../models');
    const conn = await UserConnection.create({
      user_id: req.user.id,
      server_id: req.body.server_id || null,
      connected_at: new Date()
    });
    res.json({ success: true, data: { id: conn.id } });
  } catch (error) {
    console.error(error);
    res.status(500).json({ success: false, message: 'Server error' });
  }
});

// Record a VPN session end. Body: { } — duration computed server-side.
router.put('/connections/:id/disconnect', auth, async (req, res) => {
  try {
    const { UserConnection } = require('../models');
    const conn = await UserConnection.findOne({
      where: { id: req.params.id, user_id: req.user.id }
    });
    if (!conn) {
      return res.status(404).json({ success: false, message: 'Connection not found' });
    }
    const now = new Date();
    const duration = Math.max(0, Math.round((now - conn.connected_at) / 1000));
    await conn.update({ disconnected_at: now, duration });
    res.json({ success: true, data: { duration } });
  } catch (error) {
    console.error(error);
    res.status(500).json({ success: false, message: 'Server error' });
  }
});

// User's connection history
router.get('/connections', auth, async (req, res) => {
  try {
    const { UserConnection, Server } = require('../models');
    const conns = await UserConnection.findAll({
      where: { user_id: req.user.id },
      include: [{ model: Server, attributes: ['name', 'country'] }],
      order: [['connected_at', 'DESC']],
      limit: 50
    });
    res.json({ success: true, data: conns });
  } catch (error) {
    console.error(error);
    res.status(500).json({ success: false, message: 'Server error' });
  }
});

module.exports = router;
