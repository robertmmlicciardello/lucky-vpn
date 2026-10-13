
module.exports = (sequelize, DataTypes) => {
  const Reward = sequelize.define('Reward', {
    id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true
    },
    user_id: {
      type: DataTypes.INTEGER,
      allowNull: false
    },
    type: {
      type: DataTypes.ENUM('daily', 'ad', 'referral', 'bonus', 'wheel', 'share', 'rate', 'redeem', 'invite'),
      allowNull: false
    },
    points: {
      type: DataTypes.INTEGER,
      allowNull: false
    },
    description: {
      type: DataTypes.STRING,
      allowNull: true
    },
    claimed_at: {
      type: DataTypes.DATE,
      defaultValue: DataTypes.NOW
    }
  });

  return Reward;
};
