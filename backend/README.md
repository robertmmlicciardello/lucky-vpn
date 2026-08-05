
# Lucky VPN Master - Backend API

A complete Node.js + Express backend for the Lucky VPN Master admin panel and mobile application.

## Features

- **User Authentication & Management**
- **VPN Server Management**
- **Payment Processing & Approval System**
- **Rewards & Points System**
- **OneConnect Integration**
- **Ad Management**
- **Notification System**
- **Support Ticket System**
- **Blog Management**
- **Leaderboard System**

## Installation

1. Clone the repository
2. Install dependencies:
```bash
npm install
```

3. Create a MySQL database and configure the `.env` file:
```bash
cp .env.example .env
```

4. Update the `.env` file with your database credentials and other configurations.

5. Start the server:
```bash
# Development
npm run dev

# Production
npm start
```

## API Endpoints

### Authentication
- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/admin/login` - Admin login

### Servers
- `GET /api/v1/servers` - Get all servers
- `GET /api/v1/servers/free` - Get free servers
- `GET /api/v1/servers/premium` - Get premium servers
- `POST /api/v1/servers` - Add server (Admin)
- `PUT /api/v1/servers/:id` - Update server (Admin)
- `DELETE /api/v1/servers/:id` - Delete server (Admin)

### Payments
- `POST /api/v1/subscription/submit` - Submit payment for approval
- `GET /api/v1/subscription/history` - Get payment history
- `GET /api/v1/subscription/pending` - Get pending payments (Admin)
- `POST /api/v1/subscription/:id/approve` - Approve payment (Admin)
- `POST /api/v1/subscription/:id/reject` - Reject payment (Admin)

## Default Admin Credentials

For demo purposes:
- **Email:** admin@luckyvpn.com
- **Password:** admin123

⚠️ **Important:** Change these credentials in production!

## Database Schema

The API uses MySQL with Sequelize ORM. Tables include:
- Users
- Servers
- Payments
- Rewards
- Notifications
- Support Tickets
- Blog Posts
- User Connections

## Security Features

- JWT Authentication
- Password Hashing with bcryptjs
- Rate Limiting
- CORS Protection
- Input Validation
- Helmet Security Headers

## Environment Variables

Required environment variables:
- `DB_HOST`, `DB_USER`, `DB_PASSWORD`, `DB_NAME` - Database config
- `JWT_SECRET` - JWT signing secret
- `PORT` - Server port
- `FRONTEND_URL` - Frontend URL for CORS
- Payment gateway APIs (KPay, etc.)
- Email configuration
- AdMob configuration

## Development

```bash
# Install dependencies
npm install

# Start in development mode
npm run dev

# Run tests
npm test
```

## Production Deployment

1. Set `NODE_ENV=production`
2. Configure production database
3. Set secure JWT secret
4. Configure payment gateways
5. Set up SSL/HTTPS
6. Configure reverse proxy (nginx)
7. Set up monitoring and logging

## API Integration

The mobile app and admin panel should use these endpoints. Update the `API_BASE_URL` in your frontend to point to this backend server.

Example API usage:
```javascript
const response = await fetch('http://your-api-domain.com/api/v1/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({ email, password }),
});
```
