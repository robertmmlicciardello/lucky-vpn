const express = require('express');
const { PaymentConfig } = require('../models');
const router = express.Router();

// Public payment configuration (no auth).
// Only exposes fields the client needs to display payment options:
// account numbers/names, QR image URLs, prices and enabled flags.
router.get('/', async (req, res) => {
  try {
    let config = await PaymentConfig.findByPk(1);
    if (!config) config = await PaymentConfig.create({ id: 1 });
    const c = config.toJSON();

    res.json({
      success: true,
      data: {
        kpay_account_number: c.kpay_account_number,
        kpay_account_name: c.kpay_account_name,
        kpay_qr_url: c.kpay_qr_url,
        wave_account_number: c.wave_account_number,
        wave_account_name: c.wave_account_name,
        wave_qr_url: c.wave_qr_url,
        monthly_price: c.monthly_price,
        yearly_price: c.yearly_price,
        currency: c.currency,
        kpay_enabled: c.kpay_enabled,
        wave_enabled: c.wave_enabled,
        google_play_enabled: c.google_play_enabled
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
