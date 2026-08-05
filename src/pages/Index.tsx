
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { 
  Users, 
  Server, 
  Settings, 
  DollarSign, 
  Smartphone, 
  Gift,
  BarChart3,
  Shield,
  MonitorPlay,
  Sparkles
} from "lucide-react";
import { useState } from "react";
import ServerManagement from "@/components/admin/ServerManagement";
import UserManagement from "@/components/admin/UserManagement";
import AdManagement from "@/components/admin/AdManagement";
import MultiAdNetworkManager from "@/components/admin/MultiAdNetworkManager";
import SubscriptionPlans from "@/components/admin/SubscriptionPlans";
import RewardsSystem from "@/components/admin/RewardsSystem";
import AppSettings from "@/components/admin/AppSettings";
import DashboardStats from "@/components/admin/DashboardStats";
import NotificationManagement from "@/components/admin/NotificationManagement";
import SupportSystem from "@/components/admin/SupportSystem";
import BlogManagement from "@/components/admin/BlogManagement";
import Leaderboard from "@/components/admin/Leaderboard";

const Index = () => {
  const [activeTab, setActiveTab] = useState("dashboard");

  const navigationItems = [
    { value: "dashboard", label: "Dashboard", icon: BarChart3, color: "text-blue-600", bgColor: "bg-blue-50" },
    { value: "servers", label: "Servers", icon: Server, color: "text-green-600", bgColor: "bg-green-50" },
    { value: "users", label: "Users", icon: Users, color: "text-purple-600", bgColor: "bg-purple-50" },
    { value: "ads", label: "Ads", icon: MonitorPlay, color: "text-orange-600", bgColor: "bg-orange-50" },
    { value: "multi-ads", label: "Multi Ads", icon: Shield, color: "text-red-600", bgColor: "bg-red-50" },
    { value: "plans", label: "Plans", icon: DollarSign, color: "text-yellow-600", bgColor: "bg-yellow-50" },
    { value: "rewards", label: "Rewards", icon: Gift, color: "text-pink-600", bgColor: "bg-pink-50" },
    { value: "notifications", label: "Notifications", icon: Smartphone, color: "text-indigo-600", bgColor: "bg-indigo-50" },
    { value: "support", label: "Support", icon: Settings, color: "text-cyan-600", bgColor: "bg-cyan-50" },
    { value: "blog", label: "Blog", icon: Settings, color: "text-teal-600", bgColor: "bg-teal-50" },
    { value: "leaderboard", label: "Leaderboard", icon: Settings, color: "text-violet-600", bgColor: "bg-violet-50" },
    { value: "settings", label: "Settings", icon: Settings, color: "text-gray-600", bgColor: "bg-gray-50" }
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-100 p-4 lg:p-8">
      <div className="max-w-7xl mx-auto">
        {/* Modern Header */}
        <div className="mb-8 lg:mb-12">
          <div className="bg-white/70 backdrop-blur-sm rounded-3xl p-6 lg:p-8 shadow-2xl border border-white/20">
            <div className="flex items-center gap-4 mb-4">
              <div className="w-16 h-16 bg-gradient-to-br from-blue-600 to-purple-600 rounded-2xl flex items-center justify-center shadow-lg">
                <Sparkles className="w-8 h-8 text-white" />
              </div>
              <div>
                <h1 className="text-4xl lg:text-5xl font-bold bg-gradient-to-r from-blue-600 via-purple-600 to-indigo-600 bg-clip-text text-transparent">
                  VPN Admin Panel
                </h1>
                <p className="text-gray-600 text-lg lg:text-xl mt-2">
                  Advanced management system for your VPN application
                </p>
              </div>
            </div>
            
            {/* Quick Stats */}
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mt-6">
              <div className="bg-gradient-to-br from-blue-50 to-blue-100 p-4 rounded-xl border border-blue-200">
                <div className="text-2xl font-bold text-blue-600">1,247</div>
                <div className="text-sm text-blue-600/70">Active Users</div>
              </div>
              <div className="bg-gradient-to-br from-green-50 to-green-100 p-4 rounded-xl border border-green-200">
                <div className="text-2xl font-bold text-green-600">24</div>
                <div className="text-sm text-green-600/70">Servers Online</div>
              </div>
              <div className="bg-gradient-to-br from-purple-50 to-purple-100 p-4 rounded-xl border border-purple-200">
                <div className="text-2xl font-bold text-purple-600">$2,456</div>
                <div className="text-sm text-purple-600/70">Monthly Revenue</div>
              </div>
              <div className="bg-gradient-to-br from-orange-50 to-orange-100 p-4 rounded-xl border border-orange-200">
                <div className="text-2xl font-bold text-orange-600">98.5%</div>
                <div className="text-sm text-orange-600/70">Uptime</div>
              </div>
            </div>
          </div>
        </div>

        {/* Modern Navigation Tabs - Single Row Mobile */}
        <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-8">
          <div className="bg-white/70 backdrop-blur-sm rounded-2xl p-2 shadow-xl border border-white/20">
            {/* Mobile Navigation - Single Row Horizontal Scroll */}
            <div className="block lg:hidden">
              <div className="flex gap-1 overflow-x-auto pb-2 scrollbar-hide">
                {navigationItems.map((item) => {
                  const IconComponent = item.icon;
                  return (
                    <button
                      key={item.value}
                      onClick={() => setActiveTab(item.value)}
                      className={`flex flex-col items-center gap-1 p-2 min-w-[60px] rounded-lg transition-all duration-200 whitespace-nowrap ${
                        activeTab === item.value 
                          ? 'bg-white shadow-md scale-105 border border-blue-200' 
                          : 'hover:bg-white/50'
                      }`}
                    >
                      <div className={`w-6 h-6 ${item.bgColor} rounded-md flex items-center justify-center`}>
                        <IconComponent className={`w-3 h-3 ${item.color}`} />
                      </div>
                      <span className="text-[10px] font-medium text-center leading-tight">
                        {item.label}
                      </span>
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Desktop Navigation - Grid Layout */}
            <TabsList className="hidden lg:grid w-full grid-cols-6 gap-2 bg-transparent">
              {navigationItems.map((item) => {
                const IconComponent = item.icon;
                return (
                  <TabsTrigger 
                    key={item.value}
                    value={item.value} 
                    className="flex flex-col items-center gap-2 p-4 rounded-xl transition-all duration-200 data-[state=active]:bg-white data-[state=active]:shadow-lg data-[state=active]:scale-105"
                  >
                    <div className={`w-10 h-10 ${item.bgColor} rounded-lg flex items-center justify-center shadow-sm`}>
                      <IconComponent className={`w-5 h-5 ${item.color}`} />
                    </div>
                    <span className="text-sm font-medium">
                      {item.label}
                    </span>
                  </TabsTrigger>
                );
              })}
            </TabsList>
          </div>

          {/* Tab Contents with modern styling */}
          <div className="bg-white/30 backdrop-blur-sm rounded-2xl shadow-xl border border-white/20 overflow-hidden">
            <TabsContent value="dashboard" className="m-0">
              <div className="p-6 lg:p-8">
                <DashboardStats />
              </div>
            </TabsContent>

            <TabsContent value="servers" className="m-0">
              <div className="p-6 lg:p-8">
                <ServerManagement />
              </div>
            </TabsContent>

            <TabsContent value="users" className="m-0">
              <div className="p-6 lg:p-8">
                <UserManagement />
              </div>
            </TabsContent>

            <TabsContent value="ads" className="m-0">
              <div className="p-6 lg:p-8">
                <AdManagement />
              </div>
            </TabsContent>

            <TabsContent value="multi-ads" className="m-0">
              <MultiAdNetworkManager />
            </TabsContent>

            <TabsContent value="plans" className="m-0">
              <div className="p-6 lg:p-8">
                <SubscriptionPlans />
              </div>
            </TabsContent>

            <TabsContent value="rewards" className="m-0">
              <div className="p-6 lg:p-8">
                <RewardsSystem />
              </div>
            </TabsContent>

            <TabsContent value="notifications" className="m-0">
              <div className="p-6 lg:p-8">
                <NotificationManagement />
              </div>
            </TabsContent>

            <TabsContent value="support" className="m-0">
              <div className="p-6 lg:p-8">
                <SupportSystem />
              </div>
            </TabsContent>

            <TabsContent value="blog" className="m-0">
              <div className="p-6 lg:p-8">
                <BlogManagement />
              </div>
            </TabsContent>

            <TabsContent value="leaderboard" className="m-0">
              <div className="p-6 lg:p-8">
                <Leaderboard />
              </div>
            </TabsContent>

            <TabsContent value="settings" className="m-0">
              <div className="p-6 lg:p-8">
                <AppSettings />
              </div>
            </TabsContent>
          </div>
        </Tabs>
      </div>
    </div>
  );
};

export default Index;
