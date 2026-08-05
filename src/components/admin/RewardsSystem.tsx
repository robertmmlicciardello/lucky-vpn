
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Badge } from "@/components/ui/badge";
import { 
  Gift, 
  Coins, 
  Target, 
  Calendar, 
  Users, 
  Trophy,
  Gamepad2,
  CreditCard,
  Share,
  Star
} from "lucide-react";
import { useState } from "react";
import { useToast } from "@/hooks/use-toast";

const RewardsSystem = () => {
  const { toast } = useToast();
  const [rewardSettings, setRewardSettings] = useState({
    // Points System
    loginDaily: 10,
    watchVideo: 50,
    referFriend: 100,
    shareApp: 25,
    rateApp: 75,
    
    // Games
    luckyWheelEnabled: true,
    luckyWheelCost: 100,
    ticTacToeEnabled: true,
    ticTacToeReward: 30,
    scratchCardEnabled: true,
    scratchCardCost: 50,
    
    // Rewards
    minimumPayout: 1000,
    payoutEnabled: true,
    giftCardEnabled: true,
    
    // Daily rewards
    dailyCheckInEnabled: true,
    dailyRewards: [10, 15, 20, 25, 30, 35, 50] // 7 days
  });

  const [leaderboard, setLeaderboard] = useState([
    { rank: 1, name: "John Doe", points: 5420, country: "USA" },
    { rank: 2, name: "Jane Smith", points: 4890, country: "UK" },
    { rank: 3, name: "Mike Johnson", points: 4350, country: "Canada" },
    { rank: 4, name: "Sarah Wilson", points: 3920, country: "Australia" },
    { rank: 5, name: "David Brown", points: 3650, country: "Germany" }
  ]);

  const updateRewardSetting = (key: string, value: string | number | boolean | number[]) => {
    setRewardSettings(prev => ({
      ...prev,
      [key]: value
    }));
  };

  const handleSaveSettings = () => {
    toast({
      title: "Success",
      description: "Reward settings saved successfully"
    });
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900">Rewards & Points System</h2>
        <p className="text-gray-600">Manage user rewards, points, and gamification features</p>
      </div>

      {/* Points Overview */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Total Points Distributed</p>
                <p className="text-2xl font-bold text-orange-600">124,567</p>
              </div>
              <Coins className="w-8 h-8 text-orange-500" />
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Active Gamers</p>
                <p className="text-2xl font-bold text-purple-600">3,421</p>
              </div>
              <Gamepad2 className="w-8 h-8 text-purple-500" />
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Payout Requests</p>
                <p className="text-2xl font-bold text-green-600">89</p>
              </div>
              <CreditCard className="w-8 h-8 text-green-500" />
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Today's Check-ins</p>
                <p className="text-2xl font-bold text-blue-600">2,156</p>
              </div>
              <Calendar className="w-8 h-8 text-blue-500" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Points Configuration */}
      <Card>
        <CardHeader>
          <CardTitle>Points Configuration</CardTitle>
          <CardDescription>Set point values for different user actions</CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <h3 className="font-semibold text-gray-900">User Actions</h3>
              
              <div className="flex items-center justify-between">
                <Label htmlFor="loginDaily">Daily Login</Label>
                <div className="flex items-center space-x-2">
                  <Input
                    id="loginDaily"
                    type="number"
                    min="1"
                    value={rewardSettings.loginDaily}
                    onChange={(e) => updateRewardSetting('loginDaily', parseInt(e.target.value))}
                    className="w-20"
                  />
                  <span className="text-sm text-gray-600">points</span>
                </div>
              </div>

              <div className="flex items-center justify-between">
                <Label htmlFor="watchVideo">Watch Rewarded Video</Label>
                <div className="flex items-center space-x-2">
                  <Input
                    id="watchVideo"
                    type="number"
                    min="1"
                    value={rewardSettings.watchVideo}
                    onChange={(e) => updateRewardSetting('watchVideo', parseInt(e.target.value))}
                    className="w-20"
                  />
                  <span className="text-sm text-gray-600">points</span>
                </div>
              </div>

              <div className="flex items-center justify-between">
                <Label htmlFor="referFriend">Refer a Friend</Label>
                <div className="flex items-center space-x-2">
                  <Input
                    id="referFriend"
                    type="number"
                    min="1"
                    value={rewardSettings.referFriend}
                    onChange={(e) => updateRewardSetting('referFriend', parseInt(e.target.value))}
                    className="w-20"
                  />
                  <span className="text-sm text-gray-600">points</span>
                </div>
              </div>

              <div className="flex items-center justify-between">
                <Label htmlFor="shareApp">Share App</Label>
                <div className="flex items-center space-x-2">
                  <Input
                    id="shareApp"
                    type="number"
                    min="1"
                    value={rewardSettings.shareApp}
                    onChange={(e) => updateRewardSetting('shareApp', parseInt(e.target.value))}
                    className="w-20"
                  />
                  <span className="text-sm text-gray-600">points</span>
                </div>
              </div>

              <div className="flex items-center justify-between">
                <Label htmlFor="rateApp">Rate App</Label>
                <div className="flex items-center space-x-2">
                  <Input
                    id="rateApp"
                    type="number"
                    min="1"
                    value={rewardSettings.rateApp}
                    onChange={(e) => updateRewardSetting('rateApp', parseInt(e.target.value))}
                    className="w-20"
                  />
                  <span className="text-sm text-gray-600">points</span>
                </div>
              </div>
            </div>

            <div className="space-y-4">
              <h3 className="font-semibold text-gray-900">Games & Activities</h3>
              
              <div className="flex items-center justify-between">
                <div>
                  <Label>Lucky Wheel</Label>
                  <p className="text-xs text-gray-600">Cost per spin</p>
                </div>
                <div className="flex items-center space-x-2">
                  <Switch
                    checked={rewardSettings.luckyWheelEnabled}
                    onCheckedChange={(checked) => updateRewardSetting('luckyWheelEnabled', checked)}
                  />
                  <Input
                    type="number"
                    min="1"
                    value={rewardSettings.luckyWheelCost}
                    onChange={(e) => updateRewardSetting('luckyWheelCost', parseInt(e.target.value))}
                    className="w-20"
                    disabled={!rewardSettings.luckyWheelEnabled}
                  />
                </div>
              </div>

              <div className="flex items-center justify-between">
                <div>
                  <Label>Tic-Tac-Toe</Label>
                  <p className="text-xs text-gray-600">Reward for winning</p>
                </div>
                <div className="flex items-center space-x-2">
                  <Switch
                    checked={rewardSettings.ticTacToeEnabled}
                    onCheckedChange={(checked) => updateRewardSetting('ticTacToeEnabled', checked)}
                  />
                  <Input
                    type="number"
                    min="1"
                    value={rewardSettings.ticTacToeReward}
                    onChange={(e) => updateRewardSetting('ticTacToeReward', parseInt(e.target.value))}
                    className="w-20"
                    disabled={!rewardSettings.ticTacToeEnabled}
                  />
                </div>
              </div>

              <div className="flex items-center justify-between">
                <div>
                  <Label>Scratch Card</Label>
                  <p className="text-xs text-gray-600">Cost per card</p>
                </div>
                <div className="flex items-center space-x-2">
                  <Switch
                    checked={rewardSettings.scratchCardEnabled}
                    onCheckedChange={(checked) => updateRewardSetting('scratchCardEnabled', checked)}
                  />
                  <Input
                    type="number"
                    min="1"
                    value={rewardSettings.scratchCardCost}
                    onChange={(e) => updateRewardSetting('scratchCardCost', parseInt(e.target.value))}
                    className="w-20"
                    disabled={!rewardSettings.scratchCardEnabled}
                  />
                </div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Daily Check-in Rewards */}
      <Card>
        <CardHeader>
          <CardTitle>Daily Check-in Rewards</CardTitle>
          <CardDescription>Configure daily login reward sequence</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-between mb-4">
            <Label className="text-base font-medium">Enable Daily Check-in</Label>
            <Switch
              checked={rewardSettings.dailyCheckInEnabled}
              onCheckedChange={(checked) => updateRewardSetting('dailyCheckInEnabled', checked)}
            />
          </div>
          
          <div className="grid grid-cols-7 gap-2">
            {rewardSettings.dailyRewards.map((reward, index) => (
              <div key={index} className="text-center">
                <Label className="text-sm">Day {index + 1}</Label>
                <Input
                  type="number"
                  min="1"
                  value={reward}
                  onChange={(e) => {
                    const newRewards = [...rewardSettings.dailyRewards];
                    newRewards[index] = parseInt(e.target.value);
                    updateRewardSetting('dailyRewards', newRewards);
                  }}
                  className="mt-1"
                  disabled={!rewardSettings.dailyCheckInEnabled}
                />
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Leaderboard */}
      <Card>
        <CardHeader>
          <CardTitle>Current Leaderboard</CardTitle>
          <CardDescription>Top users by points earned</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {leaderboard.map((user) => (
              <div key={user.rank} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                <div className="flex items-center space-x-3">
                  <div className="flex items-center justify-center w-8 h-8 bg-gradient-to-r from-yellow-400 to-orange-500 rounded-full text-white font-bold text-sm">
                    {user.rank}
                  </div>
                  <div>
                    <p className="font-medium text-gray-900">{user.name}</p>
                    <p className="text-sm text-gray-600">{user.country}</p>
                  </div>
                </div>
                <div className="flex items-center space-x-2">
                  <Coins className="w-4 h-4 text-orange-500" />
                  <span className="font-semibold text-orange-600">{user.points.toLocaleString()}</span>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Payout Settings */}
      <Card>
        <CardHeader>
          <CardTitle>Payout Settings</CardTitle>
          <CardDescription>Configure point redemption and payout options</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <Label className="text-base font-medium">Enable Payouts</Label>
              <p className="text-sm text-gray-600">Allow users to redeem points for money</p>
            </div>
            <Switch
              checked={rewardSettings.payoutEnabled}
              onCheckedChange={(checked) => updateRewardSetting('payoutEnabled', checked)}
            />
          </div>

          <div className="flex items-center justify-between">
            <Label htmlFor="minimumPayout">Minimum Payout Points</Label>
            <div className="flex items-center space-x-2">
              <Input
                id="minimumPayout"
                type="number"
                min="100"
                value={rewardSettings.minimumPayout}
                onChange={(e) => updateRewardSetting('minimumPayout', parseInt(e.target.value))}
                className="w-32"
                disabled={!rewardSettings.payoutEnabled}
              />
              <span className="text-sm text-gray-600">points</span>
            </div>
          </div>

          <div className="flex items-center justify-between">
            <div>
              <Label className="text-base font-medium">Enable Gift Cards</Label>
              <p className="text-sm text-gray-600">Allow redemption for gift cards</p>
            </div>
            <Switch
              checked={rewardSettings.giftCardEnabled}
              onCheckedChange={(checked) => updateRewardSetting('giftCardEnabled', checked)}
            />
          </div>
        </CardContent>
      </Card>

      <div className="flex justify-end">
        <Button onClick={handleSaveSettings} className="px-8">
          Save Reward Settings
        </Button>
      </div>
    </div>
  );
};

export default RewardsSystem;
