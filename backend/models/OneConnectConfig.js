
module.exports = (sequelize, DataTypes) => {
  const OneConnectConfig = sequelize.define('OneConnectConfig', {
    id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true
    },
    api_url: {
      type: DataTypes.STRING,
      allowNull: false
    },
    api_key: {
      type: DataTypes.STRING,
      allowNull: false
    },
    auto_sync: {
      type: DataTypes.BOOLEAN,
      defaultValue: false
    },
    sync_interval: {
      type: DataTypes.INTEGER,
      defaultValue: 60
    },
    last_sync: {
      type: DataTypes.DATE,
      allowNull: true
    },
    status: {
      type: DataTypes.ENUM('active', 'inactive'),
      defaultValue: 'active'
    }
  });

  return OneConnectConfig;
};
