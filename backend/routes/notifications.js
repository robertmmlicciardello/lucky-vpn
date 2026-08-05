
const express = require('express');
const { Notification, User } = require('../models');
const auth = require('../middleware/auth');
const adminAuth = require('../middleware/adminAuth');
const router = express.Router();

// Get user notifications
router.get('/', auth, async (req, res) => {
  try {
    const notifications = await Notification.findAll({
      where: { user_id: req.user.id },
      order: [['createdAt', 'DESC']]
    });

    res.json({
      success: true,
      data: notifications
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Mark notification as read
router.put('/:id/read', auth, async (req, res) => {
  try {
    const notification = await Notification.findOne({
      where: { 
        id: req.params.id,
        user_id: req.user.id 
      }
    });

    if (!notification) {
      return res.status(404).json({
        success: false,
        message: 'Notification not found'
      });
    }

    await notification.update({ is_read: true });

    res.json({
      success: true,
      message: 'Notification marked as read'
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Admin - Send notification
router.post('/send', adminAuth, async (req, res) => {
  try {
    const { title, message, type, target_users } = req.body;

    let users = [];
    if (target_users === 'all') {
      users = await User.findAll({ attributes: ['id'] });
    } else if (target_users === 'premium') {
      users = await User.findAll({ 
        where: { plan: 'premium' },
        attributes: ['id'] 
      });
    } else if (target_users === 'free') {
      users = await User.findAll({ 
        where: { plan: 'free' },
        attributes: ['id'] 
      });
    }

    const notifications = users.map(user => ({
      user_id: user.id,
      title,
      message,
      type: type || 'general'
    }));

    await Notification.bulkCreate(notifications);

    res.json({
      success: true,
      message: `Notification sent to ${users.length} users`
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Admin - Get all notifications
router.get('/admin/all', adminAuth, async (req, res) => {
  try {
    const notifications = await Notification.findAll({
      include: [{
        model: User,
        attributes: ['name', 'email']
      }],
      order: [['createdAt', 'DESC']],
      limit: 100
    });

    res.json({
      success: true,
      data: notifications
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
