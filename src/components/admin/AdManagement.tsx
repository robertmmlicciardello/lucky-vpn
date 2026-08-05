
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { MonitorPlay, Smartphone, Gift, Eye } from "lucide-react";
import { useState } from "react";
import { useToast } from "@/hooks/use-toast";

const AdManagement = () => {
  const { toast } = useToast();
  const [adSettings, setAdSettings] = useState({
    admobEnabled: true,
    bannerAdId: "ca-app-pub-3940256099942544/6300978111",
    interstitialAdId: "ca-app-pub-3940256099942544/1033173712", 
    rewardedAdId: "ca-app-pub-3940256099942544/5224354917",
    bannerEnabled: true,
    interstitialEnabled: true,
    rewardedEnabled: true,
    adFrequency: 3,
    rewardPoints: 50
  });

  const handleSaveAdSettings = () => {
    // Here you would typically save to your backend
    toast({
      title: "Success",
      description: "Ad settings saved successfully"
    });
  };

  const updateAdSetting = (key: string, value: string | number | boolean) => {
    setAdSettings(prev => ({
      ...prev,
      [key]: value
    }));
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900">Ad Management</h2>
        <p className="text-gray-600">Configure AdMob settings and ad placements</p>
      </div>

      {/* AdMob Configuration */}
      <Card>
        <CardHeader>
          <CardTitle>AdMob Configuration</CardTitle>
          <CardDescription>
            Configure your Google AdMob settings and ad unit IDs
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="flex items-center justify-between">
            <div>
              <Label className="text-base font-medium">Enable AdMob</Label>
              <p className="text-sm text-gray-600">Turn on/off all AdMob advertisements</p>
            </div>
            <Switch
              checked={adSettings.admobEnabled}
              onCheckedChange={(checked) => updateAdSetting('admobEnabled', checked)}
            />
          </div>

          <div className="grid gap-4">
            <div>
              <Label htmlFor="bannerAdId">Banner Ad Unit ID</Label>
              <Input
                id="bannerAdId"
                value={adSettings.bannerAdId}
                onChange={(e) => updateAdSetting('bannerAdId', e.target.value)}
                placeholder="ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx"
                disabled={!adSettings.admobEnabled}
              />
            </div>

            <div>
              <Label htmlFor="interstitialAdId">Interstitial Ad Unit ID</Label>
              <Input
                id="interstitialAdId"
                value={adSettings.interstitialAdId}
                onChange={(e) => updateAdSetting('interstitialAdId', e.target.value)}
                placeholder="ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx"
                disabled={!adSettings.admobEnabled}
              />
            </div>

            <div>
              <Label htmlFor="rewardedAdId">Rewarded Ad Unit ID</Label>
              <Input
                id="rewardedAdId"
                value={adSettings.rewardedAdId}
                onChange={(e) => updateAdSetting('rewardedAdId', e.target.value)}
                placeholder="ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx"
                disabled={!adSettings.admobEnabled}
              />
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Ad Placement Settings */}
      <Card>
        <CardHeader>
          <CardTitle>Ad Placement Settings</CardTitle>
          <CardDescription>
            Control where and how often ads are displayed
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="grid gap-6">
            <div className="flex items-center justify-between p-4 border rounded-lg">
              <div className="flex items-center space-x-3">
                <MonitorPlay className="w-8 h-8 text-blue-500" />
                <div>
                  <h3 className="font-medium">Banner Ads</h3>
                  <p className="text-sm text-gray-600">Show banner ads at bottom of screen</p>
                </div>
              </div>
              <Switch
                checked={adSettings.bannerEnabled}
                onCheckedChange={(checked) => updateAdSetting('bannerEnabled', checked)}
                disabled={!adSettings.admobEnabled}
              />
            </div>

            <div className="flex items-center justify-between p-4 border rounded-lg">
              <div className="flex items-center space-x-3">
                <Smartphone className="w-8 h-8 text-green-500" />
                <div>
                  <h3 className="font-medium">Interstitial Ads</h3>
                  <p className="text-sm text-gray-600">Full-screen ads between activities</p>
                </div>
              </div>
              <Switch
                checked={adSettings.interstitialEnabled}
                onCheckedChange={(checked) => updateAdSetting('interstitialEnabled', checked)}
                disabled={!adSettings.admobEnabled}
              />
            </div>

            <div className="flex items-center justify-between p-4 border rounded-lg">
              <div className="flex items-center space-x-3">
                <Gift className="w-8 h-8 text-purple-500" />
                <div>
                  <h3 className="font-medium">Rewarded Video Ads</h3>
                  <p className="text-sm text-gray-600">Ads that give users rewards</p>
                </div>
              </div>
              <Switch
                checked={adSettings.rewardedEnabled}
                onCheckedChange={(checked) => updateAdSetting('rewardedEnabled', checked)}
                disabled={!adSettings.admobEnabled}
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <Label htmlFor="adFrequency">Interstitial Ad Frequency</Label>
              <Input
                id="adFrequency"
                type="number"
                min="1"
                max="10"
                value={adSettings.adFrequency}
                onChange={(e) => updateAdSetting('adFrequency', parseInt(e.target.value))}
                disabled={!adSettings.admobEnabled}
              />
              <p className="text-xs text-gray-600 mt-1">Show interstitial ad every X connections</p>
            </div>

            <div>
              <Label htmlFor="rewardPoints">Reward Points per Video</Label>
              <Input
                id="rewardPoints"
                type="number"
                min="10"
                max="500"
                value={adSettings.rewardPoints}
                onChange={(e) => updateAdSetting('rewardPoints', parseInt(e.target.value))}
                disabled={!adSettings.admobEnabled}
              />
              <p className="text-xs text-gray-600 mt-1">Points awarded for watching rewarded videos</p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Ad Performance */}
      <Card>
        <CardHeader>
          <CardTitle>Ad Performance (Today)</CardTitle>
          <CardDescription>
            Current advertising statistics and revenue
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="text-center p-4 bg-blue-50 rounded-lg">
              <Eye className="w-8 h-8 text-blue-500 mx-auto mb-2" />
              <div className="text-2xl font-bold text-gray-900">1,247</div>
              <div className="text-sm text-gray-600">Banner Impressions</div>
            </div>
            <div className="text-center p-4 bg-green-50 rounded-lg">
              <MonitorPlay className="w-8 h-8 text-green-500 mx-auto mb-2" />
              <div className="text-2xl font-bold text-gray-900">89</div>
              <div className="text-sm text-gray-600">Interstitial Views</div>
            </div>
            <div className="text-center p-4 bg-purple-50 rounded-lg">
              <Gift className="w-8 h-8 text-purple-500 mx-auto mb-2" />
              <div className="text-2xl font-bold text-gray-900">156</div>
              <div className="text-sm text-gray-600">Rewarded Videos</div>
            </div>
            <div className="text-center p-4 bg-orange-50 rounded-lg">
              <div className="text-2xl font-bold text-orange-600">$24.35</div>
              <div className="text-sm text-gray-600">Est. Revenue</div>
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="flex justify-end">
        <Button onClick={handleSaveAdSettings} className="px-8">
          Save Ad Settings
        </Button>
      </div>
    </div>
  );
};

export default AdManagement;
