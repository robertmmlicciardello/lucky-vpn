
const { Sequelize } = require('sequelize');
require('dotenv').config();

const sequelize = new Sequelize(
  process.env.DB_NAME || 'lucky_vpn_master',
  process.env.DB_USER || 'root',
  process.env.DB_PASSWORD || '',
  {
    host: process.env.DB_HOST || 'localhost',
    port: process.env.DB_PORT || 3306,
    dialect: 'mysql',
    logging: process.env.NODE_ENV === 'development' ? console.log : false,
    pool: {
      max: 5,
      min: 0,
      acquire: 30000,
      idle: 10000
    }
  }
);

// Import all models
const User = require('./User')(sequelize, Sequelize.DataTypes);
const Admin = require('./Admin')(sequelize, Sequelize.DataTypes);
const Server = require('./Server')(sequelize, Sequelize.DataTypes);
const Payment = require('./Payment')(sequelize, Sequelize.DataTypes);
const Reward = require('./Reward')(sequelize, Sequelize.DataTypes);
const Notification = require('./Notification')(sequelize, Sequelize.DataTypes);
const SupportTicket = require('./SupportTicket')(sequelize, Sequelize.DataTypes);
const BlogPost = require('./BlogPost')(sequelize, Sequelize.DataTypes);
const AdConfig = require('./AdConfig')(sequelize, Sequelize.DataTypes);
const OneConnectConfig = require('./OneConnectConfig')(sequelize, Sequelize.DataTypes);
const UserConnection = require('./UserConnection')(sequelize, Sequelize.DataTypes);

// Define associations
User.hasMany(Payment, { foreignKey: 'user_id' });
Payment.belongsTo(User, { foreignKey: 'user_id' });

User.hasMany(Reward, { foreignKey: 'user_id' });
Reward.belongsTo(User, { foreignKey: 'user_id' });

User.hasMany(Notification, { foreignKey: 'user_id' });
Notification.belongsTo(User, { foreignKey: 'user_id' });

User.hasMany(SupportTicket, { foreignKey: 'user_id' });
SupportTicket.belongsTo(User, { foreignKey: 'user_id' });

User.hasMany(UserConnection, { foreignKey: 'user_id' });
UserConnection.belongsTo(User, { foreignKey: 'user_id' });

Server.hasMany(UserConnection, { foreignKey: 'server_id' });
UserConnection.belongsTo(Server, { foreignKey: 'server_id' });

Admin.hasMany(BlogPost, { foreignKey: 'author_id' });
BlogPost.belongsTo(Admin, { foreignKey: 'author_id' });

// Self-referencing for user referrals
User.hasMany(User, { as: 'referrals', foreignKey: 'referred_by' });
User.belongsTo(User, { as: 'referrer', foreignKey: 'referred_by' });

const db = {
  sequelize,
  Sequelize,
  User,
  Admin,
  Server,
  Payment,
  Reward,
  Notification,
  SupportTicket,
  BlogPost,
  AdConfig,
  OneConnectConfig,
  UserConnection
};

module.exports = db;
