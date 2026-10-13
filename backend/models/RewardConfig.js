module.exports = (sequelize, DataTypes) => {
  const RewardConfig = sequelize.define('RewardConfig', {
    id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true
    },
    daily_checkin_points: {
      type: DataTypes.INTEGER,
      allowNull: false,
      defaultValue: 100
    },
    watch_ad_points: {
      type: DataTypes.INTEGER,
      allowNull: false,
      defaultValue: 50
    },
    wheel_min_points: {
      type: DataTypes.INTEGER,
      allowNull: false,
      defaultValue: 10
    },
    wheel_max_points: {
      type: DataTypes.INTEGER,
      allowNull: false,
      defaultValue: 1000
    },
    share_points: {
      type: DataTypes.INTEGER,
      allowNull: false,
      defaultValue: 200
    },
    rate_points: {
      type: DataTypes.INTEGER,
      allowNull: false,
      defaultValue: 300
    },
    invite_points: {
      type: DataTypes.INTEGER,
      allowNull: false,
      defaultValue: 500
    },
    redeem_points_required: {
      type: DataTypes.INTEGER,
      allowNull: false,
      defaultValue: 1000
    },
    redeem_premium_days: {
      type: DataTypes.INTEGER,
      allowNull: false,
      defaultValue: 7
    }
  });

  return RewardConfig;
};
