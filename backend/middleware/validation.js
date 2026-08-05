
const { body, validationResult } = require('express-validator');

// Input validation rules
const userRegistrationRules = [
  body('name')
    .isLength({ min: 2, max: 50 })
    .withMessage('Name must be between 2 and 50 characters')
    .matches(/^[a-zA-Z\s]+$/)
    .withMessage('Name can only contain letters and spaces'),
  
  body('email')
    .isEmail()
    .withMessage('Must be a valid email address')
    .normalizeEmail(),
  
  body('password')
    .isLength({ min: 8 })
    .withMessage('Password must be at least 8 characters long')
    .matches(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/)
    .withMessage('Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character'),
  
  body('phone')
    .optional()
    .isMobilePhone()
    .withMessage('Must be a valid phone number'),
  
  body('referralCode')
    .optional()
    .isAlphanumeric()
    .isLength({ min: 6, max: 8 })
    .withMessage('Referral code must be 6-8 alphanumeric characters')
];

const userLoginRules = [
  body('email')
    .isEmail()
    .withMessage('Must be a valid email address')
    .normalizeEmail(),
  
  body('password')
    .notEmpty()
    .withMessage('Password is required')
];

const paymentSubmissionRules = [
  body('transaction_id')
    .isLength({ min: 5, max: 100 })
    .withMessage('Transaction ID must be between 5 and 100 characters')
    .matches(/^[a-zA-Z0-9_-]+$/)
    .withMessage('Transaction ID can only contain letters, numbers, hyphens, and underscores'),
  
  body('amount')
    .isFloat({ min: 0.01 })
    .withMessage('Amount must be a positive number'),
  
  body('payment_method')
    .isIn(['kpay', 'wave', 'aya', 'cb', 'bank_transfer'])
    .withMessage('Invalid payment method'),
  
  body('plan_type')
    .isIn(['1_month', '3_months', '1_year'])
    .withMessage('Invalid plan type'),
  
  body('duration')
    .isInt({ min: 1, max: 365 })
    .withMessage('Duration must be between 1 and 365 days')
];

const serverManagementRules = [
  body('name')
    .isLength({ min: 2, max: 50 })
    .withMessage('Server name must be between 2 and 50 characters'),
  
  body('host')
    .isIP()
    .withMessage('Host must be a valid IP address'),
  
  body('port')
    .isInt({ min: 1, max: 65535 })
    .withMessage('Port must be between 1 and 65535'),
  
  body('country')
    .isLength({ min: 2, max: 50 })
    .withMessage('Country must be between 2 and 50 characters'),
  
  body('type')
    .isIn(['free', 'premium'])
    .withMessage('Server type must be either free or premium')
];

// Validation error handler
const handleValidationErrors = (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({
      success: false,
      message: 'Validation failed',
      errors: errors.array()
    });
  }
  next();
};

module.exports = {
  userRegistrationRules,
  userLoginRules,
  paymentSubmissionRules,
  serverManagementRules,
  handleValidationErrors
};
