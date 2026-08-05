
module.exports = (sequelize, DataTypes) => {
  const Notification = sequelize.define('Notification', {
    id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true
    },
    user_id: {
      type: DataTypes.INTEGER,
      allowNull: false
    },
    title: {
      type: DataTypes.STRING,
      allowNull: false
    },
    message: {
      type: DataTypes.TEXT,
      allowNull: false
    },
    type: {
      type: DataTypes.ENUM('general', 'payment', 'server', 'promotion'),
      defaultValue: 'general'
    },
    is_read: {
      type: DataTypes.BOOLEAN,
      defaultValue: false
    },
    action_url: {
      type: DataTypes.STRING,
      allowNull: true
    }
  });

  return Notification;
};
