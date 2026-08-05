
const crypto = require('crypto');

// Server-side game validation to prevent cheating
class GameValidator {
  static generateGameSession(userId, gameType) {
    const timestamp = Date.now();
    const sessionData = `${userId}-${gameType}-${timestamp}`;
    const hash = crypto.createHash('sha256').update(sessionData + process.env.GAME_SECRET).digest('hex');
    
    return {
      sessionId: Buffer.from(sessionData).toString('base64'),
      hash,
      timestamp
    };
  }

  static validateGameSession(sessionId, hash, userId, gameType) {
    try {
      const sessionData = Buffer.from(sessionId, 'base64').toString();
      const [sessionUserId, sessionGameType, timestamp] = sessionData.split('-');
      
      // Validate session belongs to user and game type
      if (sessionUserId !== userId.toString() || sessionGameType !== gameType) {
        return false;
      }
      
      // Validate session is not expired (30 minutes)
      if (Date.now() - parseInt(timestamp) > 30 * 60 * 1000) {
        return false;
      }
      
      // Validate hash
      const expectedHash = crypto.createHash('sha256')
        .update(sessionData + process.env.GAME_SECRET)
        .digest('hex');
      
      return hash === expectedHash;
    } catch (error) {
      return false;
    }
  }

  static validateLuckyWheelResult(result, sessionId) {
    // Lucky wheel has predefined segments with specific probabilities
    const validResults = [10, 20, 50, 100, 200, 500]; // Points
    const probabilities = [0.4, 0.3, 0.15, 0.1, 0.04, 0.01]; // 40%, 30%, 15%, 10%, 4%, 1%
    
    if (!validResults.includes(result)) {
      return false;
    }
    
    // Additional validation can be added based on session data
    return true;
  }

  static validateScratchCardResult(result, cardType) {
    const cardConfigs = {
      'bronze': { maxReward: 100, minReward: 10 },
      'silver': { maxReward: 500, minReward: 50 },
      'gold': { maxReward: 1000, minReward: 100 }
    };
    
    const config = cardConfigs[cardType];
    if (!config) return false;
    
    return result >= config.minReward && result <= config.maxReward;
  }
}

module.exports = GameValidator;
