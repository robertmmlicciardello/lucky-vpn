
const swaggerJsdoc = require('swagger-jsdoc');
const swaggerUi = require('swagger-ui-express');

const options = {
  definition: {
    openapi: '3.0.0',
    info: {
      title: 'Monetize VPN API',
      version: '1.0.0',
      description: 'Complete VPN solution with built-in monetization features API documentation',
      contact: {
        name: 'API Support',
        email: 'support@monetizevpn.com'
      }
    },
    servers: [
      {
        url: process.env.BASE_URL || 'http://localhost:3000',
        description: 'Development server'
      }
    ],
    components: {
      securitySchemes: {
        bearerAuth: {
          type: 'http',
          scheme: 'bearer',
          bearerFormat: 'JWT'
        }
      },
      schemas: {
        User: {
          type: 'object',
          properties: {
            id: { type: 'integer' },
            name: { type: 'string' },
            email: { type: 'string', format: 'email' },
            plan: { type: 'string', enum: ['free', 'premium'] },
            points: { type: 'integer' },
            status: { type: 'string', enum: ['active', 'blocked'] },
            createdAt: { type: 'string', format: 'date-time' }
          }
        },
        Server: {
          type: 'object',
          properties: {
            id: { type: 'integer' },
            name: { type: 'string' },
            country: { type: 'string' },
            city: { type: 'string' },
            ip: { type: 'string' },
            port: { type: 'integer' },
            protocol: { type: 'string' },
            status: { type: 'string', enum: ['online', 'offline', 'maintenance'] }
          }
        },
        Payment: {
          type: 'object',
          properties: {
            id: { type: 'integer' },
            user_id: { type: 'integer' },
            amount: { type: 'number' },
            currency: { type: 'string' },
            payment_method: { type: 'string' },
            transaction_id: { type: 'string' },
            status: { type: 'string', enum: ['pending', 'approved', 'rejected'] },
            plan_type: { type: 'string' }
          }
        },
        Error: {
          type: 'object',
          properties: {
            success: { type: 'boolean', example: false },
            message: { type: 'string' }
          }
        },
        Success: {
          type: 'object',
          properties: {
            success: { type: 'boolean', example: true },
            message: { type: 'string' },
            data: { type: 'object' }
          }
        }
      }
    },
    security: [
      {
        bearerAuth: []
      }
    ]
  },
  apis: ['./routes/*.js'] // Path to the API files
};

const specs = swaggerJsdoc(options);

module.exports = {
  specs,
  swaggerUi
};
