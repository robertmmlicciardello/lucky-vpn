
const express = require('express');
const { User, Reward } = require('../models');
const auth = require('../middleware/auth');
const router = express.Router();

// Get points leaderboard
router.get('/points', auth, async (req, res) => {
  try {
    const users = await User.findAll({
      attributes: ['id', 'name', 'points'],
      order: [['points', 'DESC']],
      limit: 50
    });

    const leaderboard = users.map((user, index) => ({
      rank: index + 1,
      id: user.id,
      name: user.name,
      points: user.points
    }));

    res.json({
      success: true,
      data: leaderboard
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Get referral leaderboard
router.get('/referrals', auth, async (req, res) => {
  try {
    const users = await User.findAll({
      attributes: [
        'id', 
        'name',
        [require('sequelize').fn('COUNT', require('sequelize').col('referred_by')), 'referral_count']
      ],
      group: ['User.id'],
      order: [[require('sequelize').literal('referral_count'), 'DESC']],
      limit: 50,
      include: [{
        model: User,
        as: 'referrals',
        attributes: []
      }]
    });

    const leaderboard = users.map((user, index) => ({
      rank: index + 1,
      id: user.id,
      name: user.name,
      referrals: user.dataValues.referral_count
    }));

    res.json({
      success: true,
      data: leaderboard
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

// Get user's rank
router.get('/my-rank', auth, async (req, res) => {
  try {
    const userPoints = req.user.points;
    
    const rank = await User.count({
      where: {
        points: {
          [require('sequelize').Op.gt]: userPoints
        }
      }
    }) + 1;

    res.json({
      success: true,
      data: {
        rank,
        points: userPoints
      }
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      message: 'Server error'
    });
  }
});

module.exports = router;
