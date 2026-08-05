
const crypto = require('crypto');

class PaymentValidator {
  static validateTransactionId(transactionId, paymentMethod) {
    // Each payment method has different transaction ID formats
    const patterns = {
      'kpay': /^KP[0-9]{12,16}$/,
      'wave': /^WV[0-9A-Z]{10,14}$/,
      'aya': /^AYA[0-9]{10,15}$/,
      'cb': /^CB[0-9]{8,12}$/,
      'bank_transfer': /^BT[0-9]{10,20}$/
    };
    
    const pattern = patterns[paymentMethod];
    return pattern ? pattern.test(transactionId) : false;
  }

  static validatePaymentAmount(amount, planType) {
    const planPrices = {
      '1_month': { min: 1000, max: 5000 }, // MMK
      '3_months': { min: 2500, max: 12000 },
      '1_year': { min: 8000, max: 40000 }
    };
    
    const priceRange = planPrices[planType];
    if (!priceRange) return false;
    
    return amount >= priceRange.min && amount <= priceRange.max;
  }

  static generatePaymentHash(paymentData) {
    const { transaction_id, amount, user_id, timestamp } = paymentData;
    const dataString = `${transaction_id}-${amount}-${user_id}-${timestamp}`;
    
    return crypto.createHash('sha256')
      .update(dataString + process.env.PAYMENT_SECRET)
      .digest('hex');
  }

  static validatePaymentHash(paymentData, providedHash) {
    const expectedHash = this.generatePaymentHash(paymentData);
    return expectedHash === providedHash;
  }

  static preventDuplicateSubmission(transactionId, userId) {
    // This should be implemented with Redis or database caching
    // For now, we'll add a simple time-based check
    const submissionKey = crypto.createHash('md5')
      .update(`${transactionId}-${userId}`)
      .digest('hex');
    
    // In production, store this in Redis with expiration
    return submissionKey;
  }
}

module.exports = PaymentValidator;
