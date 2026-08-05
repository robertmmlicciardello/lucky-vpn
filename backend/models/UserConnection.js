
module.exports = (sequelize, DataTypes) => {
  const UserConnection = sequelize.define('UserConnection', {
    id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true
    },
    user_id: {
      type: DataTypes.INTEGER,
      allowNull: false
    },
    server_id: {
      type: DataTypes.INTEGER,
      allowNull: false
    },
    connected_at: {
      type: DataTypes.DATE,
      allowNull: false,
      defaultValue: DataTypes.NOW
    },
    disconnected_at: {
      type: DataTypes.DATE,
      allowNull: true
    },
    duration: {
      type: DataTypes.INTEGER,
      defaultValue: 0,
      comment: 'Duration in seconds'
    },
    bytes_sent: {
      type: DataTypes.BIGINT,
      defaultValue: 0
    },
    bytes_received: {
      type: DataTypes.BIGINT,
      defaultValue: 0
    }
  });

  return UserConnection;
};
