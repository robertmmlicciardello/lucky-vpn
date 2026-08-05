
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Badge } from "@/components/ui/badge";
import { Crown, Calendar, DollarSign, Edit, Check } from "lucide-react";
import { useState } from "react";
import { useToast } from "@/hooks/use-toast";

interface SubscriptionPlan {
  id: number;
  name: string;
  duration: string;
  price: number;
  currency: string;
  features: string[];
  active: boolean;
  subscribers: number;
}

const SubscriptionPlans = () => {
  const { toast } = useToast();
  const [plans, setPlans] = useState([
    {
      id: 1,
      name: "1 Month Premium",
      duration: "1 month",
      price: 9.99,
      currency: "USD",
      features: ["Unlimited bandwidth", "Premium servers", "No ads", "24/7 support"],
      active: true,
      subscribers: 456
    },
    {
      id: 2,
      name: "3 Month Premium", 
      duration: "3 months",
      price: 24.99,
      currency: "USD",
      features: ["Unlimited bandwidth", "Premium servers", "No ads", "24/7 support", "15% discount"],
      active: true,
      subscribers: 234
    },
    {
      id: 3,
      name: "6 Month Premium",
      duration: "6 months", 
      price: 44.99,
      currency: "USD",
      features: ["Unlimited bandwidth", "Premium servers", "No ads", "24/7 support", "25% discount"],
      active: true,
      subscribers: 189
    },
    {
      id: 4,
      name: "1 Year Premium",
      duration: "12 months",
      price: 79.99,
      currency: "USD", 
      features: ["Unlimited bandwidth", "Premium servers", "No ads", "24/7 support", "33% discount", "Priority support"],
      active: true,
      subscribers: 312
    }
  ]);

  const [editingPlan, setEditingPlan] = useState<number | null>(null);
  const [editForm, setEditForm] = useState({
    name: "",
    price: 0,
    currency: "USD"
  });

  const handleEditPlan = (plan: SubscriptionPlan) => {
    setEditingPlan(plan.id);
    setEditForm({
      name: plan.name,
      price: plan.price,
      currency: plan.currency
    });
  };

  const handleSavePlan = (planId: number) => {
    setPlans(plans.map(plan => 
      plan.id === planId 
        ? { ...plan, name: editForm.name, price: editForm.price, currency: editForm.currency }
        : plan
    ));
    setEditingPlan(null);
    toast({
      title: "Success",
      description: "Plan updated successfully"
    });
  };

  const togglePlanStatus = (planId: number) => {
    setPlans(plans.map(plan => 
      plan.id === planId 
        ? { ...plan, active: !plan.active }
        : plan
    ));
    
    const plan = plans.find(p => p.id === planId);
    toast({
      title: "Success", 
      description: `Plan ${plan?.active ? 'disabled' : 'enabled'} successfully`
    });
  };

  const totalRevenue = plans.reduce((total, plan) => total + (plan.price * plan.subscribers), 0);
  const totalSubscribers = plans.reduce((total, plan) => total + plan.subscribers, 0);

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900">Subscription Plans</h2>
        <p className="text-gray-600">Manage pricing plans and subscription options</p>
      </div>

      {/* Revenue Overview */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Total Subscribers</p>
                <p className="text-2xl font-bold text-blue-600">{totalSubscribers}</p>
              </div>
              <Crown className="w-8 h-8 text-blue-500" />
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Monthly Revenue</p>
                <p className="text-2xl font-bold text-green-600">${totalRevenue.toFixed(2)}</p>
              </div>
              <DollarSign className="w-8 h-8 text-green-500" />
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Active Plans</p>
                <p className="text-2xl font-bold text-purple-600">{plans.filter(p => p.active).length}</p>
              </div>
              <Calendar className="w-8 h-8 text-purple-500" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Subscription Plans */}
      <div className="grid gap-6">
        {plans.map((plan) => (
          <Card key={plan.id} className={`hover:shadow-md transition-shadow ${!plan.active ? 'opacity-60' : ''}`}>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-4">
                  <div className="w-12 h-12 bg-gradient-to-r from-purple-500 to-blue-500 rounded-lg flex items-center justify-center">
                    <Crown className="w-6 h-6 text-white" />
                  </div>
                  <div className="flex-1">
                    {editingPlan === plan.id ? (
                      <div className="space-y-2">
                        <Input
                          value={editForm.name}
                          onChange={(e) => setEditForm({...editForm, name: e.target.value})}
                          className="font-semibold"
                        />
                        <div className="flex space-x-2">
                          <Input
                            type="number"
                            step="0.01"
                            value={editForm.price}
                            onChange={(e) => setEditForm({...editForm, price: parseFloat(e.target.value)})}
                            className="w-24"
                          />
                          <Input
                            value={editForm.currency}
                            onChange={(e) => setEditForm({...editForm, currency: e.target.value})}
                            className="w-20"
                          />
                        </div>
                      </div>
                    ) : (
                      <>
                        <h3 className="text-lg font-semibold text-gray-900">{plan.name}</h3>
                        <div className="flex items-center space-x-2">
                          <span className="text-2xl font-bold text-green-600">
                            ${plan.price}
                          </span>
                          <span className="text-gray-600">/ {plan.duration}</span>
                          <Badge variant={plan.active ? "default" : "secondary"}>
                            {plan.active ? "Active" : "Disabled"}
                          </Badge>
                        </div>
                      </>
                    )}
                    
                    <div className="flex flex-wrap gap-2 mt-2">
                      {plan.features.map((feature, index) => (
                        <div key={index} className="flex items-center text-sm text-gray-600">
                          <Check className="w-4 h-4 text-green-500 mr-1" />
                          {feature}
                        </div>
                      ))}
                    </div>
                  </div>
                </div>

                <div className="flex items-center space-x-6">
                  <div className="text-center">
                    <div className="text-lg font-semibold text-gray-900">{plan.subscribers}</div>
                    <span className="text-sm text-gray-600">Subscribers</span>
                  </div>
                  
                  <div className="text-center">
                    <div className="text-lg font-semibold text-green-600">
                      ${(plan.price * plan.subscribers).toFixed(2)}
                    </div>
                    <span className="text-sm text-gray-600">Revenue</span>
                  </div>

                  <div className="flex items-center space-x-2">
                    <Switch
                      checked={plan.active}
                      onCheckedChange={() => togglePlanStatus(plan.id)}
                    />
                    
                    {editingPlan === plan.id ? (
                      <div className="flex space-x-2">
                        <Button 
                          size="sm" 
                          onClick={() => handleSavePlan(plan.id)}
                        >
                          Save
                        </Button>
                        <Button 
                          variant="outline" 
                          size="sm" 
                          onClick={() => setEditingPlan(null)}
                        >
                          Cancel
                        </Button>
                      </div>
                    ) : (
                      <Button 
                        variant="outline" 
                        size="sm"
                        onClick={() => handleEditPlan(plan)}
                      >
                        <Edit className="w-4 h-4" />
                      </Button>
                    )}
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* In-App Purchase Settings */}
      <Card>
        <CardHeader>
          <CardTitle>In-App Purchase Settings</CardTitle>
          <CardDescription>
            Configure payment processing and subscription settings
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <Label htmlFor="googlePlayKey">Google Play Console Key</Label>
              <Input
                id="googlePlayKey"
                type="password"
                placeholder="Your Google Play Console API Key"
              />
            </div>
            <div>
              <Label htmlFor="stripeKey">Stripe Secret Key</Label>
              <Input
                id="stripeKey"
                type="password"
                placeholder="Your Stripe Secret Key"
              />
            </div>
          </div>
          
          <div className="flex items-center justify-between">
            <div>
              <Label className="text-base font-medium">Free Trial Period</Label>
              <p className="text-sm text-gray-600">Allow users to try premium features for free</p>
            </div>
            <div className="flex items-center space-x-2">
              <Input
                type="number"
                min="1"
                max="30"
                defaultValue="7"
                className="w-20"
              />
              <span className="text-sm text-gray-600">days</span>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default SubscriptionPlans;
