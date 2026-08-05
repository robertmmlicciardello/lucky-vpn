
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Users, Server, DollarSign, Smartphone, TrendingUp, Globe } from "lucide-react";

const DashboardStats = () => {
  const stats = [
    {
      title: "Total Users",
      value: "12,847",
      change: "+12.5%",
      icon: Users,
      color: "text-blue-600"
    },
    {
      title: "Active Servers",
      value: "24",
      change: "+2",
      icon: Server,
      color: "text-green-600"
    },
    {
      title: "Premium Users",
      value: "3,421",
      change: "+8.2%",
      icon: DollarSign,
      color: "text-purple-600"
    },
    {
      title: "App Downloads",
      value: "45,672",
      change: "+15.3%",
      icon: Smartphone,
      color: "text-orange-600"
    },
    {
      title: "Revenue (Monthly)",
      value: "$8,947",
      change: "+22.1%",
      icon: TrendingUp,
      color: "text-emerald-600"
    },
    {
      title: "Countries Served",
      value: "15",
      change: "+1",
      icon: Globe,
      color: "text-indigo-600"
    }
  ];

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {stats.map((stat, index) => (
          <Card key={index} className="hover:shadow-lg transition-shadow">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-gray-600">
                {stat.title}
              </CardTitle>
              <stat.icon className={`h-4 w-4 ${stat.color}`} />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-gray-900">{stat.value}</div>
              <p className="text-xs text-green-600 mt-1">
                {stat.change} from last month
              </p>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Recent Activity */}
      <Card>
        <CardHeader>
          <CardTitle>Recent Activity</CardTitle>
          <CardDescription>Latest updates from your VPN network</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="flex items-center justify-between p-3 bg-green-50 rounded-lg">
              <div>
                <p className="font-medium text-gray-900">New premium user registered</p>
                <p className="text-sm text-gray-600">user@example.com - Singapore Premium Plan</p>
              </div>
              <span className="text-xs text-gray-500">2 mins ago</span>
            </div>
            <div className="flex items-center justify-between p-3 bg-blue-50 rounded-lg">
              <div>
                <p className="font-medium text-gray-900">Server added successfully</p>
                <p className="text-sm text-gray-600">Tokyo Server - Premium Tier</p>
              </div>
              <span className="text-xs text-gray-500">15 mins ago</span>
            </div>
            <div className="flex items-center justify-between p-3 bg-yellow-50 rounded-lg">
              <div>
                <p className="font-medium text-gray-900">High server load detected</p>
                <p className="text-sm text-gray-600">US East Server - 85% capacity</p>
              </div>
              <span className="text-xs text-gray-500">1 hour ago</span>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default DashboardStats;
