
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Trophy, Medal, Award, Crown, Star, TrendingUp, Users, Gift } from "lucide-react";
import { useState } from "react";
import { useToast } from "@/hooks/use-toast";

interface LeaderboardUser {
  id: number;
  name: string;
  email: string;
  points: number;
  rank: number;
  country: string;
  plan: 'free' | 'premium';
  referrals: number;
  daysActive: number;
  achievements: string[];
}

interface Achievement {
  id: string;
  name: string;
  description: string;
  icon: string;
  pointsRequired: number;
  category: 'points' | 'referrals' | 'activity' | 'premium';
}

const Leaderboard = () => {
  const { toast } = useToast();
  const [selectedPeriod, setSelectedPeriod] = useState<'daily' | 'weekly' | 'monthly' | 'all-time'>('all-time');
  const [selectedCategory, setSelectedCategory] = useState<'points' | 'referrals' | 'activity'>('points');
  
  const [users, setUsers] = useState<LeaderboardUser[]>([
    {
      id: 1,
      name: "John Doe",
      email: "john@example.com",
      points: 15420,
      rank: 1,
      country: "United States",
      plan: 'premium',
      referrals: 25,
      daysActive: 180,
      achievements: ['pioneer', 'referrer', 'premium']
    },
    {
      id: 2,
      name: "Jane Smith",
      email: "jane@example.com", 
      points: 12350,
      rank: 2,
      country: "United Kingdom",
      plan: 'premium',
      referrals: 18,
      daysActive: 150,
      achievements: ['consistent', 'premium']
    },
    {
      id: 3,
      name: "Mike Johnson",
      email: "mike@example.com",
      points: 9870,
      rank: 3,
      country: "Canada",
      plan: 'free',
      referrals: 12,
      daysActive: 120,
      achievements: ['active', 'referrer']
    },
    {
      id: 4,
      name: "Sarah Wilson",
      email: "sarah@example.com",
      points: 8450,
      rank: 4,
      country: "Australia",
      plan: 'premium',
      referrals: 8,
      daysActive: 90,
      achievements: ['premium']
    },
    {
      id: 5,
      name: "David Brown",
      email: "david@example.com",
      points: 7230,
      rank: 5,
      country: "Germany",
      plan: 'free',
      referrals: 15,
      daysActive: 85,
      achievements: ['referrer']
    }
  ]);

  const [achievements] = useState<Achievement[]>([
    {
      id: 'pioneer',
      name: 'Pioneer',
      description: 'One of the first 100 users',
      icon: '🏆',
      pointsRequired: 0,
      category: 'activity'
    },
    {
      id: 'referrer',
      name: 'Super Referrer',
      description: 'Referred 10+ friends',
      icon: '🤝',
      pointsRequired: 0,
      category: 'referrals'
    },
    {
      id: 'premium',
      name: 'Premium Member',
      description: 'Subscribed to premium plan',
      icon: '👑',
      pointsRequired: 0,
      category: 'premium'
    },
    {
      id: 'consistent',
      name: 'Consistent User',
      description: 'Active for 100+ days',
      icon: '⭐',
      pointsRequired: 0,
      category: 'activity'
    },
    {
      id: 'active',
      name: 'Active User',
      description: 'Daily app usage',
      icon: '🔥',
      pointsRequired: 0,
      category: 'activity'
    }
  ]);

  const getRankIcon = (rank: number) => {
    switch (rank) {
      case 1: return <Trophy className="w-6 h-6 text-yellow-500" />;
      case 2: return <Medal className="w-6 h-6 text-gray-400" />;
      case 3: return <Award className="w-6 h-6 text-amber-600" />;
      default: return <span className="w-6 h-6 flex items-center justify-center text-gray-500 font-bold">#{rank}</span>;
    }
  };

  const getRankBadgeColor = (rank: number) => {
    switch (rank) {
      case 1: return 'bg-gradient-to-r from-yellow-400 to-yellow-600';
      case 2: return 'bg-gradient-to-r from-gray-300 to-gray-500';
      case 3: return 'bg-gradient-to-r from-amber-400 to-amber-600';
      default: return 'bg-gradient-to-r from-blue-400 to-blue-600';
    }
  };

  const getAchievementIcon = (achievementId: string) => {
    const achievement = achievements.find(a => a.id === achievementId);
    return achievement?.icon || '🏅';
  };

  const getSortedUsers = () => {
    const sortedUsers = [...users];
    switch (selectedCategory) {
      case 'referrals':
        return sortedUsers.sort((a, b) => b.referrals - a.referrals);
      case 'activity':
        return sortedUsers.sort((a, b) => b.daysActive - a.daysActive);
      default:
        return sortedUsers.sort((a, b) => b.points - a.points);
    }
  };

  const handleRewardTop = () => {
    toast({
      title: "Success",
      description: "Top 10 users have been rewarded with bonus points!"
    });
  };

  const handleResetLeaderboard = () => {
    toast({
      title: "Success",
      description: "Leaderboard has been reset for the new period"
    });
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Leaderboard</h2>
          <p className="text-gray-600">Track top users and manage competitions</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={handleRewardTop}>
            <Gift className="w-4 h-4 mr-2" />
            Reward Top 10
          </Button>
          <Button variant="outline" onClick={handleResetLeaderboard}>
            <TrendingUp className="w-4 h-4 mr-2" />  
            Reset Period
          </Button>
        </div>
      </div>

      {/* Filters */}
      <div className="flex gap-4">
        <Card className="flex-1">
          <CardContent className="p-4">
            <div className="flex gap-4">
              <div>
                <label className="text-sm font-medium mb-2 block">Time Period</label>
                <Select value={selectedPeriod} onValueChange={(value: typeof selectedPeriod) => setSelectedPeriod(value)}>
                  <SelectTrigger className="w-40">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="daily">Daily</SelectItem>
                    <SelectItem value="weekly">Weekly</SelectItem>
                    <SelectItem value="monthly">Monthly</SelectItem>
                    <SelectItem value="all-time">All Time</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div>
                <label className="text-sm font-medium mb-2 block">Category</label>
                <Select value={selectedCategory} onValueChange={(value: typeof selectedCategory) => setSelectedCategory(value)}>
                  <SelectTrigger className="w-40">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="points">Points</SelectItem>
                    <SelectItem value="referrals">Referrals</SelectItem>
                    <SelectItem value="activity">Activity</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardContent className="p-4 text-center">
            <div className="text-2xl font-bold text-blue-600">{users.length}</div>
            <div className="text-sm text-gray-600">Total Participants</div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4 text-center">
            <div className="text-2xl font-bold text-purple-600">{users.filter(u => u.plan === 'premium').length}</div>
            <div className="text-sm text-gray-600">Premium Users</div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4 text-center">
            <div className="text-2xl font-bold text-green-600">{users.reduce((sum, user) => sum + user.points, 0).toLocaleString()}</div>
            <div className="text-sm text-gray-600">Total Points</div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4 text-center">
            <div className="text-2xl font-bold text-orange-600">{users.reduce((sum, user) => sum + user.referrals, 0)}</div>
            <div className="text-sm text-gray-600">Total Referrals</div>
          </CardContent>
        </Card>
      </div>

      {/* Top 3 Podium */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Trophy className="w-6 h-6 text-yellow-500" />
            Top Performers
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {getSortedUsers().slice(0, 3).map((user, index) => (
              <div key={user.id} className={`relative p-6 rounded-xl text-white text-center ${getRankBadgeColor(index + 1)}`}>
                <div className="absolute -top-3 left-1/2 transform -translate-x-1/2">
                  {getRankIcon(index + 1)}
                </div>
                <Avatar className="w-16 h-16 mx-auto mb-4 ring-4 ring-white">
                  <AvatarImage src={`https://api.dicebear.com/6/initials/svg?seed=${user.name}`} />
                  <AvatarFallback>{user.name.charAt(0)}</AvatarFallback>
                </Avatar>
                <h3 className="font-bold text-lg">{user.name}</h3>
                <p className="text-sm opacity-90">{user.country}</p>
                <div className="mt-2">
                  {selectedCategory === 'points' && (
                    <div className="text-2xl font-bold">{user.points.toLocaleString()}</div>
                  )}
                  {selectedCategory === 'referrals' && (
                    <div className="text-2xl font-bold">{user.referrals}</div>
                  )}
                  {selectedCategory === 'activity' && (
                    <div className="text-2xl font-bold">{user.daysActive}</div>
                  )}
                  <div className="text-xs opacity-75">
                    {selectedCategory === 'points' && 'Points'}
                    {selectedCategory === 'referrals' && 'Referrals'}
                    {selectedCategory === 'activity' && 'Days Active'}
                  </div>
                </div>
                {user.plan === 'premium' && (
                  <Crown className="w-5 h-5 absolute top-2 right-2" />
                )}
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Full Leaderboard */}
      <Card>
        <CardHeader>
          <CardTitle>Full Leaderboard</CardTitle>
          <CardDescription>Complete ranking of all users</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {getSortedUsers().map((user, index) => (
              <div key={user.id} className="flex items-center justify-between p-4 border rounded-lg hover:bg-gray-50">
                <div className="flex items-center space-x-4">
                  <div className="flex items-center justify-center w-10 h-10">
                    {getRankIcon(index + 1)}
                  </div>
                  <Avatar>
                    <AvatarImage src={`https://api.dicebear.com/6/initials/svg?seed=${user.name}`} />
                    <AvatarFallback>{user.name.charAt(0)}</AvatarFallback>
                  </Avatar>
                  <div>
                    <div className="flex items-center gap-2">
                      <h3 className="font-semibold text-gray-900">{user.name}</h3>
                      {user.plan === 'premium' && (
                        <Crown className="w-4 h-4 text-yellow-500" />
                      )}
                    </div>
                    <p className="text-sm text-gray-600">{user.email}</p>
                    <p className="text-xs text-gray-500">{user.country}</p>
                  </div>
                </div>

                <div className="flex items-center space-x-6">
                  <div className="text-center">
                    <div className="text-lg font-bold text-blue-600">{user.points.toLocaleString()}</div>
                    <div className="text-xs text-gray-500">Points</div>
                  </div>
                  
                  <div className="text-center">
                    <div className="text-lg font-bold text-green-600">{user.referrals}</div>
                    <div className="text-xs text-gray-500">Referrals</div>
                  </div>

                  <div className="text-center">
                    <div className="text-lg font-bold text-purple-600">{user.daysActive}</div>
                    <div className="text-xs text-gray-500">Days</div>
                  </div>

                  <div className="flex gap-1">
                    {user.achievements.map(achievementId => (
                      <span key={achievementId} className="text-lg" title={achievements.find(a => a.id === achievementId)?.name}>
                        {getAchievementIcon(achievementId)}
                      </span>
                    ))}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Achievements */}
      <Card>
        <CardHeader>
          <CardTitle>Available Achievements</CardTitle>
          <CardDescription>Badges users can earn</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {achievements.map((achievement) => (
              <div key={achievement.id} className="flex items-center gap-3 p-3 border rounded-lg">
                <span className="text-2xl">{achievement.icon}</span>
                <div>
                  <h4 className="font-semibold">{achievement.name}</h4>
                  <p className="text-sm text-gray-600">{achievement.description}</p>
                  <Badge variant="outline" className="mt-1">
                    {achievement.category}
                  </Badge>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default Leaderboard;
