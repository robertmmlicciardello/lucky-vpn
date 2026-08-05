
module.exports = (sequelize, DataTypes) => {
  const AdConfig = sequelize.define('AdConfig', {
    id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true
    },
    admob_app_id: {
      type: DataTypes.STRING,
      allowNull: true
    },
    admob_banner_id: {
      type: DataTypes.STRING,
      allowNull: true
    },
    admob_interstitial_id: {
      type: DataTypes.STRING,
      allowNull: true
    },
    admob_rewarded_id: {
      type: DataTypes.STRING,
      allowNull: true
    },
    facebook_app_id: {
      type: DataTypes.STRING,
      allowNull: true
    },
    facebook_banner_id: {
      type: DataTypes.STRING,
      allowNull: true
    },
    facebook_interstitial_id: {
      type: DataTypes.STRING,
      allowNull: true
    },
    facebook_rewarded_id: {
      type: DataTypes.STRING,
      allowNull: true
    },
    unity_game_id: {
      type: DataTypes.STRING,
      allowNull: true
    },
    unity_banner_id: {
      type: DataTypes.STRING,
      allowNull: true
    },
    unity_interstitial_id: {
      type: DataTypes.STRING,
      allowNull: true
    },
    unity_rewarded_id: {
      type: DataTypes.STRING,
      allowNull: true
    },
    primary_network: {
      type: DataTypes.ENUM('admob', 'facebook', 'unity'),
      defaultValue: 'admob'
    },
    show_ads: {
      type: DataTypes.BOOLEAN,
      defaultValue: true
    },
    ad_frequency: {
      type: DataTypes.INTEGER,
      defaultValue: 3
    }
  });

  return AdConfig;
};
