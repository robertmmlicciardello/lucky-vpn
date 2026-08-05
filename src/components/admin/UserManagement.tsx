
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Search, Ban, CheckCircle, Shield, Crown, Users } from "lucide-react";
import { useState } from "react";
import { useToast } from "@/hooks/use-toast";

const UserManagement = () => {
  const { toast } = useToast();
  const [searchTerm, setSearchTerm] = useState("");
  const [users, setUsers] = useState([
    {
      id: 1,
      name: "John Doe",
      email: "john@example.com",
      phone: "+1234567890",
      plan: "Premium",
      points: 1250,
      status: "Active",
      joinDate: "2024-01-15",
      lastSeen: "2024-06-12",
      country: "United States"
    },
    {
      id: 2,
      name: "Jane Smith", 
      email: "jane@example.com",
      phone: "+0987654321",
      plan: "Free",
      points: 320,
      status: "Active",
      joinDate: "2024-02-20",
      lastSeen: "2024-06-11",
      country: "United Kingdom"
    },
    {
      id: 3,
      name: "Mike Johnson",
      email: "mike@example.com", 
      phone: "+1122334455",
      plan: "Premium",
      points: 2100,
      status: "Blocked",
      joinDate: "2024-01-10",
      lastSeen: "2024-06-10",
      country: "Canada"
    }
  ]);

  const filteredUsers = users.filter(user =>
    user.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    user.email.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleBlockUser = (userId: number) => {
    setUsers(users.map(user => 
      user.id === userId 
        ? { ...user, status: user.status === "Active" ? "Blocked" : "Active" }
        : user
    ));
    
    const user = users.find(u => u.id === userId);
    toast({
      title: "Success",
      description: `User ${user?.status === "Active" ? "blocked" : "unblocked"} successfully`
    });
  };

  const getStatusColor = (status: string) => {
    return status === "Active" ? "bg-green-500" : "bg-red-500";
  };

  const getPlanIcon = (plan: string) => {
    return plan === "Premium" ? <Crown className="w-4 h-4" /> : <Users className="w-4 h-4" />;
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">User Management</h2>
          <p className="text-gray-600">Manage registered users and their accounts</p>
        </div>
      </div>

      {/* Search and Filter */}
      <Card>
        <CardHeader>
          <CardTitle>Search Users</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex space-x-4">
            <div className="flex-1 relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
              <Input
                placeholder="Search by name or email..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10"
              />
            </div>
          </div>
        </CardContent>
      </Card>

      {/* User Statistics */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Total Users</p>
                <p className="text-2xl font-bold">{users.length}</p>
              </div>
              <Users className="w-8 h-8 text-blue-500" />
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Active Users</p>
                <p className="text-2xl font-bold text-green-600">
                  {users.filter(u => u.status === "Active").length}
                </p>
              </div>
              <CheckCircle className="w-8 h-8 text-green-500" />
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Premium Users</p>
                <p className="text-2xl font-bold text-purple-600">
                  {users.filter(u => u.plan === "Premium").length}
                </p>
              </div>
              <Crown className="w-8 h-8 text-purple-500" />
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Blocked Users</p>
                <p className="text-2xl font-bold text-red-600">
                  {users.filter(u => u.status === "Blocked").length}
                </p>
              </div>
              <Ban className="w-8 h-8 text-red-500" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* User List */}
      <Card>
        <CardHeader>
          <CardTitle>All Users</CardTitle>
          <CardDescription>Manage your application users</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {filteredUsers.map((user) => (
              <div key={user.id} className="flex items-center justify-between p-4 border rounded-lg hover:bg-gray-50">
                <div className="flex items-center space-x-4">
                  <Avatar>
                    <AvatarImage src={`https://api.dicebear.com/6/initials/svg?seed=${user.name}`} />
                    <AvatarFallback>{user.name.charAt(0)}</AvatarFallback>
                  </Avatar>
                  <div>
                    <h3 className="font-semibold text-gray-900">{user.name}</h3>
                    <p className="text-sm text-gray-600">{user.email}</p>
                    <p className="text-sm text-gray-500">{user.phone}</p>
                  </div>
                </div>

                <div className="flex items-center space-x-6">
                  <div className="text-center">
                    <div className={`w-3 h-3 rounded-full ${getStatusColor(user.status)} mb-1`}></div>
                    <span className="text-xs text-gray-600">{user.status}</span>
                  </div>
                  
                  <div className="text-center">
                    <div className="flex items-center gap-1">
                      {getPlanIcon(user.plan)}
                      <span className="text-sm font-medium">{user.plan}</span>
                    </div>
                    <span className="text-xs text-gray-600">Plan</span>
                  </div>

                  <div className="text-center">
                    <div className="text-sm font-semibold text-orange-600">{user.points}</div>
                    <span className="text-xs text-gray-600">Points</span>
                  </div>

                  <div className="text-center">
                    <div className="text-sm text-gray-900">{user.country}</div>
                    <span className="text-xs text-gray-600">Location</span>
                  </div>

                  <div className="text-center">
                    <div className="text-sm text-gray-900">{user.lastSeen}</div>
                    <span className="text-xs text-gray-600">Last Seen</span>
                  </div>

                  <Button
                    variant={user.status === "Active" ? "destructive" : "default"}
                    size="sm"
                    onClick={() => handleBlockUser(user.id)}
                  >
                    {user.status === "Active" ? (
                      <>
                        <Ban className="w-4 h-4 mr-1" />
                        Block
                      </>
                    ) : (
                      <>
                        <CheckCircle className="w-4 h-4 mr-1" />
                        Unblock
                      </>
                    )}
                  </Button>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default UserManagement;
