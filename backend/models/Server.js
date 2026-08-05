
module.exports = (sequelize, DataTypes) => {
  const Server = sequelize.define('Server', {
    id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true
    },
    name: {
      type: DataTypes.STRING,
      allowNull: false
    },
    country: {
      type: DataTypes.STRING,
      allowNull: false
    },
    city: {
      type: DataTypes.STRING,
      allowNull: true
    },
    ip: {
      type: DataTypes.STRING,
      allowNull: false
    },
    port: {
      type: DataTypes.STRING,
      defaultValue: '1194'
    },
    protocol: {
      type: DataTypes.ENUM('OpenVPN', 'IKEv2', 'WireGuard'),
      defaultValue: 'OpenVPN'
    },
    type: {
      type: DataTypes.ENUM('free', 'premium'),
      defaultValue: 'free'
    },
    status: {
      type: DataTypes.ENUM('online', 'offline', 'maintenance'),
      defaultValue: 'online'
    },
    load: {
      type: DataTypes.INTEGER,
      defaultValue: 0
    },
    users: {
      type: DataTypes.INTEGER,
      defaultValue: 0
    },
    config_file: {
      type: DataTypes.TEXT,
      allowNull: true
    },
    provider: {
      type: DataTypes.ENUM('manual', 'oneconnect'),
      defaultValue: 'manual'
    },
    oneconnect_id: {
      type: DataTypes.STRING,
      allowNull: true
    },
    last_sync: {
      type: DataTypes.DATE,
      allowNull: true
    }
  });

  return Server;
};
