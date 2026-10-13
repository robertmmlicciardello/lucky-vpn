
module.exports = (sequelize, DataTypes) => {
  const AppConfig = sequelize.define('AppConfig', {
    id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true
    },
    maintenance_mode: {
      type: DataTypes.BOOLEAN,
      defaultValue: false
    },
    maintenance_message: {
      type: DataTypes.TEXT,
      defaultValue: ''
    },
    min_app_version: {
      type: DataTypes.STRING,
      defaultValue: '1.0.0'
    },
    latest_app_version: {
      type: DataTypes.STRING,
      defaultValue: '1.0.0'
    },
    update_url: {
      type: DataTypes.STRING,
      defaultValue: ''
    },
    app_name: {
      type: DataTypes.STRING,
      defaultValue: 'Lucky VPN'
    },
    support_email: {
      type: DataTypes.STRING,
      defaultValue: ''
    },
    privacy_policy_url: {
      type: DataTypes.STRING,
      defaultValue: ''
    },
    terms_of_service_url: {
      type: DataTypes.STRING,
      defaultValue: ''
    },
    support_url: {
      type: DataTypes.STRING,
      defaultValue: ''
    },
    banner_image_url: {
      type: DataTypes.STRING,
      defaultValue: ''
    },
    free_server_limit: {
      type: DataTypes.INTEGER,
      defaultValue: 5
    },
    push_notifications_enabled: {
      type: DataTypes.BOOLEAN,
      defaultValue: true
    },
    update_notifications_enabled: {
      type: DataTypes.BOOLEAN,
      defaultValue: true
    },
    promo_notifications_enabled: {
      type: DataTypes.BOOLEAN,
      defaultValue: false
    }
  });

  return AppConfig;
};
