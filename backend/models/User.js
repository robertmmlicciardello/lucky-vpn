
module.exports = (sequelize, DataTypes) => {
  const User = sequelize.define('User', {
    id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true
    },
    name: {
      type: DataTypes.STRING,
      allowNull: false
    },
    email: {
      type: DataTypes.STRING,
      allowNull: false,
      unique: true,
      validate: {
        isEmail: true
      }
    },
    password: {
      type: DataTypes.STRING,
      allowNull: false
    },
    phone: {
      type: DataTypes.STRING,
      allowNull: true
    },
    plan: {
      type: DataTypes.ENUM('free', 'premium'),
      defaultValue: 'free'
    },
    points: {
      type: DataTypes.INTEGER,
      defaultValue: 0
    },
    status: {
      type: DataTypes.ENUM('active', 'blocked'),
      defaultValue: 'active'
    },
    subscription_expires: {
      type: DataTypes.DATE,
      allowNull: true
    },
    last_seen: {
      type: DataTypes.DATE,
      defaultValue: DataTypes.NOW
    },
    device_id: {
      type: DataTypes.STRING,
      allowNull: true
    },
    referral_code: {
      type: DataTypes.STRING,
      unique: true
    },
    referred_by: {
      type: DataTypes.INTEGER,
      allowNull: true
    }
  });

  return User;
};
