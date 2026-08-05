
const express = require('express');
const { Payment, User } = require('../models');
const auth = require('../middleware/auth');
const adminAuth = require('../middleware/adminAuth');
const router = express.Router();

// Submit payment for approval
router.post('/submit', auth, async (req, res) => {
  try {
    const { transaction_id, amount, payment_method, gateway_account, plan_type, duration, screenshot } = req.body;
    const user_id = req.user.id;

    // Check if transaction ID already exists
    const existingPayment = await Payment.findOne({ where: { transaction_id } });
    if (existingPayment) {
      return res.status(400).json({
        success: false,
        message: 'Transaction ID already exists'
      });
    }

    const payment = await Payment.create({
      user_id,
      transaction_id,
      amount,
      payment_method,
      gateway_account,
      plan_type,
      duration,
      screenshot,
      status: 'pending'
    });

    res.status(201).json({
      success: true,
      message: 'Payment submitted for approval',
      data: payment
    });

  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Get user's payment history
router.get('/history', auth, async (req, res) => {
  try {
    const payments = await Payment.findAll({
      where: { user_id: req.user.id },
      order: [['createdAt', 'DESC']]
    });

    res.json({
      success: true,
      data: payments
    });

  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Admin - Get all pending payments
router.get('/pending', adminAuth, async (req, res) => {
  try {
    const payments = await Payment.findAll({
      where: { status: 'pending' },
      include: [{
        model: User,
        attributes: ['name', 'email']
      }],
      order: [['createdAt', 'DESC']]
    });

    res.json({
      success: true,
      data: payments
    });

  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Admin - Approve payment
router.post('/:id/approve', adminAuth, async (req, res) => {
  try {
    const { id } = req.params;
    const { admin_notes } = req.body;

    const payment = await Payment.findByPk(id, {
      include: [User]
    });

    if (!payment) {
      return res.status(404).json({
        success: false,
        message: 'Payment not found'
      });
    }

    if (payment.status !== 'pending') {
      return res.status(400).json({
        success: false,
        message: 'Payment is not pending'
      });
    }

    // Update payment status
    await payment.update({
      status: 'approved',
      admin_notes,
      approved_at: new Date(),
      approved_by: req.user.id
    });

    // Update user subscription
    const user = payment.User;
    const currentExpiry = user.subscription_expires ? new Date(user.subscription_expires) : new Date();
    const newExpiry = new Date(Math.max(currentExpiry.getTime(), Date.now()));
    newExpiry.setDate(newExpiry.getDate() + payment.duration);

    await user.update({
      plan: 'premium',
      subscription_expires: newExpiry
    });

    res.json({
      success: true,
      message: 'Payment approved successfully'
    });

  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Admin - Reject payment
router.post('/:id/reject', adminAuth, async (req, res) => {
  try {
    const { id } = req.params;
    const { admin_notes } = req.body;

    const payment = await Payment.findByPk(id);
    if (!payment) {
      return res.status(404).json({
        success: false,
        message: 'Payment not found'
      });
    }

    await payment.update({
      status: 'rejected',
      admin_notes
    });

    res.json({
      success: true,
      message: 'Payment rejected'
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
