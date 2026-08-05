
const express = require('express');
const { SupportTicket, User } = require('../models');
const auth = require('../middleware/auth');
const adminAuth = require('../middleware/adminAuth');
const router = express.Router();

// Create support ticket
router.post('/tickets', auth, async (req, res) => {
  try {
    const { subject, message, category } = req.body;

    const ticket = await SupportTicket.create({
      user_id: req.user.id,
      subject,
      message,
      category: category || 'general',
      status: 'open'
    });

    res.status(201).json({
      success: true,
      message: 'Support ticket created',
      data: ticket
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Get user tickets
router.get('/tickets', auth, async (req, res) => {
  try {
    const tickets = await SupportTicket.findAll({
      where: { user_id: req.user.id },
      order: [['createdAt', 'DESC']]
    });

    res.json({
      success: true,
      data: tickets
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Admin - Get all tickets
router.get('/admin/tickets', adminAuth, async (req, res) => {
  try {
    const { status } = req.query;
    let whereClause = {};
    
    if (status) {
      whereClause.status = status;
    }

    const tickets = await SupportTicket.findAll({
      where: whereClause,
      include: [{
        model: User,
        attributes: ['name', 'email']
      }],
      order: [['createdAt', 'DESC']]
    });

    res.json({
      success: true,
      data: tickets
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Admin - Update ticket status
router.put('/admin/tickets/:id', adminAuth, async (req, res) => {
  try {
    const { id } = req.params;
    const { status, admin_response } = req.body;

    const ticket = await SupportTicket.findByPk(id);
    if (!ticket) {
      return res.status(404).json({
        success: false,
        message: 'Ticket not found'
      });
    }

    await ticket.update({
      status,
      admin_response,
      resolved_at: status === 'resolved' ? new Date() : null
    });

    res.json({
      success: true,
      message: 'Ticket updated successfully'
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
