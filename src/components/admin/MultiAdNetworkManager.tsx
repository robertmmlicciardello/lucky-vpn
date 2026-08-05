
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { 
  MonitorPlay, 
  Smartphone, 
  Gift, 
  Eye, 
  TrendingUp,
  Settings2,
  Facebook,
  Youtube,
  Zap,
  Target
} from "lucide-react";
import { useState } from "react";
import { useToast } from "@/hooks/use-toast";
import { AdNetworkType, AdNetworkConfiguration } from "@/api/endpoints";

const MultiAdNetworkManager = () => {
  const { toast } = useToast();
  const [adConfig, setAdConfig] = useState<AdNetworkConfiguration>({
    admob: {
      enabled: true,
      app_id: "ca-app-pub-3940256099942544~3347511713",
      banner_id: "ca-app-pub-3940256099942544/6300978111",
      interstitial_id: "ca-app-pub-3940256099942544/1033173712",
      rewarded_id: "ca-app-pub-3940256099942544/5224354917",
      app_open_id: "ca-app-pub-3940256099942544/9257395921"
    },
    facebook: {
      enabled: false,
      placement_id: "",
      banner_id: "",
      interstitial_id: "",
      rewarded_id: "",
      app_open_id: ""
    },
    applovin: {
      enabled: false,
      api_key: "",
      banner_id: "",
      interstitial_id: "",
      rewarded_id: "",
      app_open_id: ""
    },
    unity: {
      enabled: false,
      app_id: "",
      banner_id: "",
      interstitial_id: "",
      rewarded_id: "",
      app_open_id: ""
    },
    adcolony: {
      enabled: false,
      app_id: "",
      banner_id: "",
      interstitial_id: "",
      rewarded_id: "",
      app_open_id: ""
    },
    startapp: {
      enabled: false,
      app_id: "",
      banner_id: "",
      interstitial_id: "",
      rewarded_id: "",
      app_open_id: ""
    },
    primary_rewarded_network: 'admob',
    primary_interstitial_network: 'admob',
    primary_banner_network: 'admob',
    primary_app_open_network: 'admob',
    // Specific network settings for different features
    lucky_wheel_network: 'admob',
    tic_tac_toe_network: 'admob',
    scratch_card_network: 'admob',
    premium_server_unlock_network: 'admob',
    ad_frequency: 3,
    reward_points: 50
  });

  const adNetworks = [
    { 
      id: 'admob' as AdNetworkType, 
      name: 'Google AdMob', 
      icon: MonitorPlay, 
      color: 'text-green-600',
      bgColor: 'bg-green-50',
      borderColor: 'border-green-200'
    },
    { 
      id: 'facebook' as AdNetworkType, 
      name: 'Facebook Audience Network', 
      icon: Facebook, 
      color: 'text-blue-600',
      bgColor: 'bg-blue-50',
      borderColor: 'border-blue-200'
    },
    { 
      id: 'applovin' as AdNetworkType, 
      name: 'AppLovin MAX', 
      icon: TrendingUp, 
      color: 'text-purple-600',
      bgColor: 'bg-purple-50',
      borderColor: 'border-purple-200'
    },
    { 
      id: 'unity' as AdNetworkType, 
      name: 'Unity Ads', 
      icon: Smartphone, 
      color: 'text-red-600',
      bgColor: 'bg-red-50',
      borderColor: 'border-red-200'
    },
    { 
      id: 'adcolony' as AdNetworkType, 
      name: 'AdColony', 
      icon: Eye, 
      color: 'text-orange-600',
      bgColor: 'bg-orange-50',
      borderColor: 'border-orange-200'
    },
    { 
      id: 'startapp' as AdNetworkType, 
      name: 'StartApp', 
      icon: Youtube, 
      color: 'text-indigo-600',
      bgColor: 'bg-indigo-50',
      borderColor: 'border-indigo-200'
    }
  ];

  const handleSaveConfig = () => {
    toast({
      title: "Success",
      description: "Ad network configuration saved successfully"
    });
  };

  const updateNetworkConfig = (network: AdNetworkType, field: string, value: string | number | boolean) => {
    setAdConfig(prev => ({
      ...prev,
      [network]: {
        ...prev[network],
        [field]: value
      }
    }));
  };

  const updatePrimaryNetwork = (adType: 'rewarded' | 'interstitial' | 'banner' | 'app_open', network: AdNetworkType) => {
    const fieldName = `primary_${adType}_network` as keyof AdNetworkConfiguration;
    setAdConfig(prev => ({
      ...prev,
      [fieldName]: network
    }));
  };

  const updateFeatureNetwork = (feature: string, network: AdNetworkType) => {
    setAdConfig(prev => ({
      ...prev,
      [feature]: network
    }));
  };

  const renderNetworkConfig = (network: typeof adNetworks[0]) => {
    const config = adConfig[network.id];
    const IconComponent = network.icon;

    return (
      <Card key={network.id} className={`transition-all hover:shadow-lg ${network.borderColor} border-2`}>
        <CardHeader className="pb-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
              <div className={`w-12 h-12 ${network.bgColor} rounded-xl flex items-center justify-center shadow-sm`}>
                <IconComponent className={`w-6 h-6 ${network.color}`} />
              </div>
              <div>
                <CardTitle className="text-xl font-bold">{network.name}</CardTitle>
                <CardDescription className="text-sm">Configure {network.name} settings</CardDescription>
              </div>
            </div>
            <Switch
              checked={config.enabled}
              onCheckedChange={(enabled) => updateNetworkConfig(network.id, 'enabled', enabled)}
              className="data-[state=checked]:bg-primary"
            />
          </div>
        </CardHeader>
        <CardContent className="space-y-6">
          {config.enabled && (
            <>
              {network.id === 'admob' && (
                <div className="space-y-2">
                  <Label htmlFor={`${network.id}-app-id`} className="text-sm font-medium">App ID</Label>
                  <Input
                    id={`${network.id}-app-id`}
                    value={config.app_id || ''}
                    onChange={(e) => updateNetworkConfig(network.id, 'app_id', e.target.value)}
                    placeholder="ca-app-pub-xxxxxxxxxxxxxxxx~xxxxxxxxxx"
                    className="transition-all focus:ring-2 focus:ring-primary/20"
                  />
                </div>
              )}

              {network.id === 'facebook' && (
                <div className="space-y-2">
                  <Label htmlFor={`${network.id}-placement-id`} className="text-sm font-medium">Placement ID</Label>
                  <Input
                    id={`${network.id}-placement-id`}
                    value={config.placement_id || ''}
                    onChange={(e) => updateNetworkConfig(network.id, 'placement_id', e.target.value)}
                    placeholder="YOUR_PLACEMENT_ID"
                    className="transition-all focus:ring-2 focus:ring-primary/20"
                  />
                </div>
              )}

              {network.id === 'applovin' && (
                <div className="space-y-2">
                  <Label htmlFor={`${network.id}-api-key`} className="text-sm font-medium">SDK Key</Label>
                  <Input
                    id={`${network.id}-api-key`}
                    value={config.api_key || ''}
                    onChange={(e) => updateNetworkConfig(network.id, 'api_key', e.target.value)}
                    placeholder="YOUR_SDK_KEY"
                    className="transition-all focus:ring-2 focus:ring-primary/20"
                  />
                </div>
              )}

              {(network.id === 'unity' || network.id === 'adcolony' || network.id === 'startapp') && (
                <div className="space-y-2">
                  <Label htmlFor={`${network.id}-app-id`} className="text-sm font-medium">App ID</Label>
                  <Input
                    id={`${network.id}-app-id`}
                    value={config.app_id || ''}
                    onChange={(e) => updateNetworkConfig(network.id, 'app_id', e.target.value)}
                    placeholder="Your App ID"
                    className="transition-all focus:ring-2 focus:ring-primary/20"
                  />
                </div>
              )}

              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                <div className="space-y-2">
                  <Label htmlFor={`${network.id}-banner`} className="text-sm font-medium">Banner Ad ID</Label>
                  <Input
                    id={`${network.id}-banner`}
                    value={config.banner_id || ''}
                    onChange={(e) => updateNetworkConfig(network.id, 'banner_id', e.target.value)}
                    placeholder="Banner Unit ID"
                    className="transition-all focus:ring-2 focus:ring-primary/20"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor={`${network.id}-interstitial`} className="text-sm font-medium">Interstitial Ad ID</Label>
                  <Input
                    id={`${network.id}-interstitial`}
                    value={config.interstitial_id || ''}
                    onChange={(e) => updateNetworkConfig(network.id, 'interstitial_id', e.target.value)}
                    placeholder="Interstitial Unit ID"
                    className="transition-all focus:ring-2 focus:ring-primary/20"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor={`${network.id}-rewarded`} className="text-sm font-medium">Rewarded Ad ID</Label>
                  <Input
                    id={`${network.id}-rewarded`}
                    value={config.rewarded_id || ''}
                    onChange={(e) => updateNetworkConfig(network.id, 'rewarded_id', e.target.value)}
                    placeholder="Rewarded Unit ID"
                    className="transition-all focus:ring-2 focus:ring-primary/20"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor={`${network.id}-app-open`} className="text-sm font-medium">App Open Ad ID</Label>
                  <Input
                    id={`${network.id}-app-open`}
                    value={config.app_open_id || ''}
                    onChange={(e) => updateNetworkConfig(network.id, 'app_open_id', e.target.value)}
                    placeholder="App Open Unit ID"
                    className="transition-all focus:ring-2 focus:ring-primary/20"
                  />
                </div>
              </div>
            </>
          )}
        </CardContent>
      </Card>
    );
  };

  const enabledNetworks = adNetworks.filter(network => adConfig[network.id].enabled);

  return (
    <div className="space-y-8 p-6 bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 min-h-screen">
      <div className="bg-white/70 backdrop-blur-sm rounded-2xl p-6 shadow-xl border border-white/20">
        <h2 className="text-3xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent mb-2">
          Multi Ad Network Management
        </h2>
        <p className="text-gray-600 text-lg">Configure multiple ad networks and control ad placements across your VPN app</p>
      </div>

      <Tabs defaultValue="networks" className="space-y-6">
        <TabsList className="grid w-full grid-cols-4 bg-white/80 backdrop-blur-sm p-1 rounded-xl shadow-lg border border-white/20">
          <TabsTrigger value="networks" className="rounded-lg font-medium">Ad Networks</TabsTrigger>
          <TabsTrigger value="primary" className="rounded-lg font-medium">Primary Networks</TabsTrigger>
          <TabsTrigger value="features" className="rounded-lg font-medium">Feature Controls</TabsTrigger>
          <TabsTrigger value="performance" className="rounded-lg font-medium">Performance</TabsTrigger>
        </TabsList>

        <TabsContent value="networks" className="space-y-6">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {adNetworks.map(renderNetworkConfig)}
          </div>
        </TabsContent>

        <TabsContent value="primary" className="space-y-6">
          <Card className="bg-white/80 backdrop-blur-sm shadow-xl border border-white/20">
            <CardHeader>
              <CardTitle className="text-2xl font-bold flex items-center gap-3">
                <Target className="w-7 h-7 text-primary" />
                Primary Ad Network Selection
              </CardTitle>
              <CardDescription className="text-base">
                Choose which ad network to use as primary for different ad types
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-8">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <div className="space-y-3">
                  <Label className="text-sm font-semibold text-gray-700">Primary Rewarded Video Network</Label>
                  <Select
                    value={adConfig.primary_rewarded_network}
                    onValueChange={(value: AdNetworkType) => updatePrimaryNetwork('rewarded', value)}
                  >
                    <SelectTrigger className="bg-white shadow-sm border-2">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {enabledNetworks.map(network => (
                        <SelectItem key={network.id} value={network.id}>
                          {network.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-3">
                  <Label className="text-sm font-semibold text-gray-700">Primary Interstitial Network</Label>
                  <Select
                    value={adConfig.primary_interstitial_network}
                    onValueChange={(value: AdNetworkType) => updatePrimaryNetwork('interstitial', value)}
                  >
                    <SelectTrigger className="bg-white shadow-sm border-2">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {enabledNetworks.map(network => (
                        <SelectItem key={network.id} value={network.id}>
                          {network.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-3">
                  <Label className="text-sm font-semibold text-gray-700">Primary Banner Network</Label>
                  <Select
                    value={adConfig.primary_banner_network}
                    onValueChange={(value: AdNetworkType) => updatePrimaryNetwork('banner', value)}
                  >
                    <SelectTrigger className="bg-white shadow-sm border-2">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {enabledNetworks.map(network => (
                        <SelectItem key={network.id} value={network.id}>
                          {network.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-3">
                  <Label className="text-sm font-semibold text-gray-700">Primary App Open Network</Label>
                  <Select
                    value={adConfig.primary_app_open_network}
                    onValueChange={(value: AdNetworkType) => updatePrimaryNetwork('app_open', value)}
                  >
                    <SelectTrigger className="bg-white shadow-sm border-2">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {enabledNetworks.map(network => (
                        <SelectItem key={network.id} value={network.id}>
                          {network.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6 pt-6 border-t border-gray-200">
                <div className="space-y-2">
                  <Label htmlFor="ad-frequency" className="text-sm font-semibold text-gray-700">Interstitial Ad Frequency</Label>
                  <Input
                    id="ad-frequency"
                    type="number"
                    min="1"
                    max="10"
                    value={adConfig.ad_frequency}
                    onChange={(e) => setAdConfig(prev => ({ ...prev, ad_frequency: parseInt(e.target.value) }))}
                    className="bg-white shadow-sm border-2"
                  />
                  <p className="text-xs text-gray-500 mt-1">Show interstitial ad every X connections</p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="reward-points" className="text-sm font-semibold text-gray-700">Reward Points per Video</Label>
                  <Input
                    id="reward-points"
                    type="number"
                    min="10"
                    max="500"
                    value={adConfig.reward_points}
                    onChange={(e) => setAdConfig(prev => ({ ...prev, reward_points: parseInt(e.target.value) }))}
                    className="bg-white shadow-sm border-2"
                  />
                  <p className="text-xs text-gray-500 mt-1">Points awarded for watching rewarded videos</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="features" className="space-y-6">
          <Card className="bg-white/80 backdrop-blur-sm shadow-xl border border-white/20">
            <CardHeader>
              <CardTitle className="text-2xl font-bold flex items-center gap-3">
                <Zap className="w-7 h-7 text-primary" />
                Feature-Specific Ad Network Control
              </CardTitle>
              <CardDescription className="text-base">
                Control which ad network shows for specific features in your VPN app
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-8">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                <div className="space-y-6">
                  <h3 className="text-lg font-semibold text-gray-800 border-b pb-2">Game Features</h3>
                  
                  <div className="space-y-3">
                    <Label className="text-sm font-semibold text-gray-700">Lucky Wheel Ad Network</Label>
                    <Select
                      value={adConfig.lucky_wheel_network}
                      onValueChange={(value: AdNetworkType) => updateFeatureNetwork('lucky_wheel_network', value)}
                    >
                      <SelectTrigger className="bg-white shadow-sm border-2">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {enabledNetworks.map(network => (
                          <SelectItem key={network.id} value={network.id}>
                            {network.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <p className="text-xs text-gray-500">Ad network for Lucky Wheel rewards</p>
                  </div>

                  <div className="space-y-3">
                    <Label className="text-sm font-semibold text-gray-700">Tic-Tac-Toe Ad Network</Label>
                    <Select
                      value={adConfig.tic_tac_toe_network}
                      onValueChange={(value: AdNetworkType) => updateFeatureNetwork('tic_tac_toe_network', value)}
                    >
                      <SelectTrigger className="bg-white shadow-sm border-2">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {enabledNetworks.map(network => (
                          <SelectItem key={network.id} value={network.id}>
                            {network.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <p className="text-xs text-gray-500">Ad network for Tic-Tac-Toe game rewards</p>
                  </div>

                  <div className="space-y-3">
                    <Label className="text-sm font-semibold text-gray-700">Scratch Card Ad Network</Label>
                    <Select
                      value={adConfig.scratch_card_network}
                      onValueChange={(value: AdNetworkType) => updateFeatureNetwork('scratch_card_network', value)}
                    >
                      <SelectTrigger className="bg-white shadow-sm border-2">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {enabledNetworks.map(network => (
                          <SelectItem key={network.id} value={network.id}>
                            {network.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <p className="text-xs text-gray-500">Ad network for Scratch Card rewards</p>
                  </div>
                </div>

                <div className="space-y-6">
                  <h3 className="text-lg font-semibold text-gray-800 border-b pb-2">VPN Features</h3>
                  
                  <div className="space-y-3">
                    <Label className="text-sm font-semibold text-gray-700">Premium Server Unlock Ad Network</Label>
                    <Select
                      value={adConfig.premium_server_unlock_network}
                      onValueChange={(value: AdNetworkType) => updateFeatureNetwork('premium_server_unlock_network', value)}
                    >
                      <SelectTrigger className="bg-white shadow-sm border-2">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {enabledNetworks.map(network => (
                          <SelectItem key={network.id} value={network.id}>
                            {network.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <p className="text-xs text-gray-500">Ad network for premium server access via ads</p>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="performance" className="space-y-6">
          <Card className="bg-white/80 backdrop-blur-sm shadow-xl border border-white/20">
            <CardHeader>
              <CardTitle className="text-2xl font-bold">Ad Performance Overview</CardTitle>
              <CardDescription className="text-base">
                Performance statistics across all ad networks
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <div className="text-center p-6 bg-gradient-to-br from-blue-50 to-blue-100 rounded-xl shadow-lg border border-blue-200">
                  <Eye className="w-10 h-10 text-blue-600 mx-auto mb-3" />
                  <div className="text-3xl font-bold text-gray-900">2,547</div>
                  <div className="text-sm text-gray-600 font-medium">Total Impressions</div>
                </div>
                <div className="text-center p-6 bg-gradient-to-br from-green-50 to-green-100 rounded-xl shadow-lg border border-green-200">
                  <MonitorPlay className="w-10 h-10 text-green-600 mx-auto mb-3" />
                  <div className="text-3xl font-bold text-gray-900">189</div>
                  <div className="text-sm text-gray-600 font-medium">Interstitial Views</div>
                </div>
                <div className="text-center p-6 bg-gradient-to-br from-purple-50 to-purple-100 rounded-xl shadow-lg border border-purple-200">
                  <Gift className="w-10 h-10 text-purple-600 mx-auto mb-3" />
                  <div className="text-3xl font-bold text-gray-900">356</div>
                  <div className="text-sm text-gray-600 font-medium">Rewarded Videos</div>
                </div>
                <div className="text-center p-6 bg-gradient-to-br from-orange-50 to-orange-100 rounded-xl shadow-lg border border-orange-200">
                  <div className="text-3xl font-bold text-orange-600">$45.82</div>
                  <div className="text-sm text-gray-600 font-medium">Est. Revenue</div>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="bg-white/80 backdrop-blur-sm shadow-xl border border-white/20">
            <CardHeader>
              <CardTitle className="text-xl font-bold">Network Performance Breakdown</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {enabledNetworks.map(network => {
                  const IconComponent = network.icon;
                  return (
                    <div key={network.id} className="flex items-center justify-between p-6 bg-gradient-to-r from-gray-50 to-gray-100 rounded-xl shadow-md border border-gray-200 hover:shadow-lg transition-all">
                      <div className="flex items-center space-x-4">
                        <div className={`w-10 h-10 ${network.bgColor} rounded-lg flex items-center justify-center shadow-sm`}>
                          <IconComponent className={`w-5 h-5 ${network.color}`} />
                        </div>
                        <span className="font-semibold text-gray-800">{network.name}</span>
                      </div>
                      <div className="flex space-x-8 text-sm">
                        <div className="text-center">
                          <div className="text-xs text-gray-500 mb-1">Impressions</div>
                          <div className="font-semibold text-gray-900">
                            {Math.floor(Math.random() * 1000 + 100)}
                          </div>
                        </div>
                        <div className="text-center">
                          <div className="text-xs text-gray-500 mb-1">Revenue</div>
                          <div className="font-semibold text-green-600">
                            ${(Math.random() * 20 + 5).toFixed(2)}
                          </div>
                        </div>
                        <div className="text-center">
                          <div className="text-xs text-gray-500 mb-1">Fill Rate</div>
                          <div className="font-semibold text-blue-600">
                            {(Math.random() * 30 + 70).toFixed(1)}%
                          </div>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      <div className="flex justify-end">
        <Button onClick={handleSaveConfig} className="px-10 py-3 text-lg font-semibold bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 shadow-xl rounded-xl">
          <Settings2 className="w-5 h-5 mr-3" />
          Save Ad Configuration
        </Button>
      </div>
    </div>
  );
};

export default MultiAdNetworkManager;
