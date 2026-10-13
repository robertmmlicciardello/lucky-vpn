module.exports = (sequelize, DataTypes) => {
  const PaymentConfig = sequelize.define('PaymentConfig', {
    id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true
    },
    kpay_account_number: {
      type: DataTypes.STRING,
      allowNull: true
    },
    kpay_account_name: {
      type: DataTypes.STRING,
      allowNull: true
    },
    kpay_qr_url: {
      type: DataTypes.STRING,
      allowNull: true
    },
    wave_account_number: {
      type: DataTypes.STRING,
      allowNull: true
    },
    wave_account_name: {
      type: DataTypes.STRING,
      allowNull: true
    },
    wave_qr_url: {
      type: DataTypes.STRING,
      allowNull: true
    },
    monthly_price: {
      type: DataTypes.DECIMAL(10, 2),
      allowNull: true,
      defaultValue: 0
    },
    yearly_price: {
      type: DataTypes.DECIMAL(10, 2),
      allowNull: true,
      defaultValue: 0
    },
    currency: {
      type: DataTypes.STRING(10),
      allowNull: true,
      defaultValue: 'MMK'
    },
    kpay_enabled: {
      type: DataTypes.BOOLEAN,
      defaultValue: true
    },
    wave_enabled: {
      type: DataTypes.BOOLEAN,
      defaultValue: true
    },
    google_play_enabled: {
      type: DataTypes.BOOLEAN,
      defaultValue: true
    }
  });

  return PaymentConfig;
};
