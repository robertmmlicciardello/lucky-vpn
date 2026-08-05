
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Switch } from "@/components/ui/switch";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { 
  Settings, 
  Smartphone, 
  Shield, 
  Bell, 
  Globe, 
  Lock,
  FileText,
  Image,
  Mail
} from "lucide-react";
import { useState } from "react";
import { useToast } from "@/hooks/use-toast";

const AppSettings = () => {
  const { toast } = useToast();
  const [appSettings, setAppSettings] = useState({
    // App Information
    appName: "VPN Master Pro",
    appVersion: "1.2.0",
    packageName: "com.vpnmaster.pro",
    supportEmail: "support@vpnmaster.com",
    
    // Features
    forceUpdate: false,
    maintenanceMode: false,
    freeServerLimit: 3,
    connectionTimeout: 30,
    autoConnectEnabled: true,
    killSwitchEnabled: true,
    
    // Notifications
    pushNotificationsEnabled: true,
    updateNotifications: true,
    promoNotifications: false,
    
    // Security
    encryptionProtocol: "AES-256",
    loggingEnabled: false,
    crashReportsEnabled: true,
    
    // Content
    privacyPolicyUrl: "https://vpnmaster.com/privacy",
    termsOfServiceUrl: "https://vpnmaster.com/terms", 
    supportUrl: "https://vpnmaster.com/support",
    
    // UI
    defaultTheme: "light",
    bannerImageUrl: "",
    appIconUrl: "",
    
    // API Settings
    apiBaseUrl: "https://api.vpnmaster.com",
    apiVersion: "v1",
    rateLimitPerMinute: 60
  });

  const updateSetting = (key: string, value: string | number | boolean) => {
    setAppSettings(prev => ({
      ...prev,
      [key]: value
    }));
  };

  const handleSaveSettings = () => {
    // Here you would typically save to your backend
    toast({
      title: "Success",
      description: "App settings saved successfully"
    });
  };

  const handleForceUpdate = () => {
    toast({
      title: "Force Update Triggered",
      description: "All users will be prompted to update the app"
    });
  };

  const toggleMaintenanceMode = () => {
    updateSetting('maintenanceMode', !appSettings.maintenanceMode);
    toast({
      title: appSettings.maintenanceMode ? "Maintenance Mode Disabled" : "Maintenance Mode Enabled",
      description: appSettings.maintenanceMode 
        ? "App is now accessible to all users" 
        : "App is now in maintenance mode"
    });
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900">App Settings</h2>
        <p className="text-gray-600">Configure global application settings and preferences</p>
      </div>

      {/* Critical Controls */}
      <Card className="border-orange-200 bg-orange-50">
        <CardHeader>
          <CardTitle className="text-orange-800">Critical Controls</CardTitle>
          <CardDescription className="text-orange-700">
            Important settings that affect all users immediately
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <Label className="text-base font-medium text-orange-800">Maintenance Mode</Label>
              <p className="text-sm text-orange-700">Temporarily disable app access for all users</p>
            </div>
            <div className="flex items-center space-x-2">
              <Switch
                checked={appSettings.maintenanceMode}
                onCheckedChange={toggleMaintenanceMode}
              />
              {appSettings.maintenanceMode && (
                <span className="text-sm font-medium text-orange-600">ACTIVE</span>
              )}
            </div>
          </div>

          <div className="flex items-center justify-between">
            <div>
              <Label className="text-base font-medium text-orange-800">Force Update</Label>
              <p className="text-sm text-orange-700">Require users to update to latest version</p>
            </div>
            <Button
              variant={appSettings.forceUpdate ? "destructive" : "outline"}
              onClick={handleForceUpdate}
            >
              {appSettings.forceUpdate ? "Update Required" : "Trigger Update"}
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* App Information */}
      <Card>
        <CardHeader>
          <CardTitle>App Information</CardTitle>
          <CardDescription>Basic app details and metadata</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <Label htmlFor="appName">App Name</Label>
              <Input
                id="appName"
                value={appSettings.appName}
                onChange={(e) => updateSetting('appName', e.target.value)}
              />
            </div>
            <div>
              <Label htmlFor="appVersion">App Version</Label>
              <Input
                id="appVersion"
                value={appSettings.appVersion}
                onChange={(e) => updateSetting('appVersion', e.target.value)}
              />
            </div>
            <div>
              <Label htmlFor="packageName">Package Name</Label>
              <Input
                id="packageName"
                value={appSettings.packageName}
                onChange={(e) => updateSetting('packageName', e.target.value)}
              />
            </div>
            <div>
              <Label htmlFor="supportEmail">Support Email</Label>
              <Input
                id="supportEmail"
                type="email"
                value={appSettings.supportEmail}
                onChange={(e) => updateSetting('supportEmail', e.target.value)}
              />
            </div>
          </div>
        </CardContent>
      </Card>

      {/* VPN Features */}
      <Card>
        <CardHeader>
          <CardTitle>VPN Features</CardTitle>
          <CardDescription>Configure VPN functionality and limits</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <Label className="text-base font-medium">Auto-Connect</Label>
                <Switch
                  checked={appSettings.autoConnectEnabled}
                  onCheckedChange={(checked) => updateSetting('autoConnectEnabled', checked)}
                />
              </div>

              <div className="flex items-center justify-between">
                <Label className="text-base font-medium">Kill Switch</Label>
                <Switch
                  checked={appSettings.killSwitchEnabled}
                  onCheckedChange={(checked) => updateSetting('killSwitchEnabled', checked)}
                />
              </div>

              <div>
                <Label htmlFor="freeServerLimit">Free Server Limit</Label>
                <Input
                  id="freeServerLimit"
                  type="number"
                  min="1"
                  max="10"
                  value={appSettings.freeServerLimit}
                  onChange={(e) => updateSetting('freeServerLimit', parseInt(e.target.value))}
                />
              </div>
            </div>

            <div className="space-y-4">
              <div>
                <Label htmlFor="connectionTimeout">Connection Timeout (seconds)</Label>
                <Input
                  id="connectionTimeout"
                  type="number"
                  min="10"
                  max="120"
                  value={appSettings.connectionTimeout}
                  onChange={(e) => updateSetting('connectionTimeout', parseInt(e.target.value))}
                />
              </div>

              <div>
                <Label htmlFor="encryptionProtocol">Encryption Protocol</Label>
                <Select 
                  value={appSettings.encryptionProtocol} 
                  onValueChange={(value) => updateSetting('encryptionProtocol', value)}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="AES-256">AES-256</SelectItem>
                    <SelectItem value="AES-128">AES-128</SelectItem>
                    <SelectItem value="ChaCha20">ChaCha20</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Notifications */}
      <Card>
        <CardHeader>
          <CardTitle>Notification Settings</CardTitle>
          <CardDescription>Configure push notifications and alerts</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <Label className="text-base font-medium">Push Notifications</Label>
              <p className="text-sm text-gray-600">Enable general push notifications</p>
            </div>
            <Switch
              checked={appSettings.pushNotificationsEnabled}
              onCheckedChange={(checked) => updateSetting('pushNotificationsEnabled', checked)}
            />
          </div>

          <div className="flex items-center justify-between">
            <div>
              <Label className="text-base font-medium">Update Notifications</Label>
              <p className="text-sm text-gray-600">Notify users about app updates</p>
            </div>
            <Switch
              checked={appSettings.updateNotifications}
              onCheckedChange={(checked) => updateSetting('updateNotifications', checked)}
            />
          </div>

          <div className="flex items-center justify-between">
            <div>
              <Label className="text-base font-medium">Promotional Notifications</Label>
              <p className="text-sm text-gray-600">Send promotional and marketing messages</p>
            </div>
            <Switch
              checked={appSettings.promoNotifications}
              onCheckedChange={(checked) => updateSetting('promoNotifications', checked)}
            />
          </div>
        </CardContent>
      </Card>

      {/* Content & URLs */}
      <Card>
        <CardHeader>
          <CardTitle>Content & URLs</CardTitle>
          <CardDescription>Configure app content and external links</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div>
            <Label htmlFor="privacyPolicyUrl">Privacy Policy URL</Label>
            <Input
              id="privacyPolicyUrl"
              type="url"
              value={appSettings.privacyPolicyUrl}
              onChange={(e) => updateSetting('privacyPolicyUrl', e.target.value)}
            />
          </div>

          <div>
            <Label htmlFor="termsOfServiceUrl">Terms of Service URL</Label>
            <Input
              id="termsOfServiceUrl"
              type="url"
              value={appSettings.termsOfServiceUrl}
              onChange={(e) => updateSetting('termsOfServiceUrl', e.target.value)}
            />
          </div>

          <div>
            <Label htmlFor="supportUrl">Support URL</Label>
            <Input
              id="supportUrl"
              type="url"
              value={appSettings.supportUrl}
              onChange={(e) => updateSetting('supportUrl', e.target.value)}
            />
          </div>

          <div>
            <Label htmlFor="bannerImageUrl">Banner Image URL</Label>
            <Input
              id="bannerImageUrl"
              type="url"
              value={appSettings.bannerImageUrl}
              onChange={(e) => updateSetting('bannerImageUrl', e.target.value)}
              placeholder="https://example.com/banner.jpg"
            />
          </div>
        </CardContent>
      </Card>

      {/* API Settings */}
      <Card>
        <CardHeader>
          <CardTitle>API Configuration</CardTitle>
          <CardDescription>Configure API endpoints and rate limiting</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <Label htmlFor="apiBaseUrl">API Base URL</Label>
              <Input
                id="apiBaseUrl"
                type="url"
                value={appSettings.apiBaseUrl}
                onChange={(e) => updateSetting('apiBaseUrl', e.target.value)}
              />
            </div>
            <div>
              <Label htmlFor="apiVersion">API Version</Label>
              <Select 
                value={appSettings.apiVersion} 
                onValueChange={(value) => updateSetting('apiVersion', value)}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="v1">v1</SelectItem>
                  <SelectItem value="v2">v2</SelectItem>
                  <SelectItem value="v3">v3</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          <div>
            <Label htmlFor="rateLimitPerMinute">Rate Limit (requests per minute)</Label>
            <Input
              id="rateLimitPerMinute"
              type="number"
              min="10"
              max="1000"
              value={appSettings.rateLimitPerMinute}
              onChange={(e) => updateSetting('rateLimitPerMinute', parseInt(e.target.value))}
            />
          </div>

          <div className="flex items-center justify-between">
            <div>
              <Label className="text-base font-medium">Crash Reports</Label>
              <p className="text-sm text-gray-600">Collect crash reports for debugging</p>
            </div>
            <Switch
              checked={appSettings.crashReportsEnabled}
              onCheckedChange={(checked) => updateSetting('crashReportsEnabled', checked)}
            />
          </div>
        </CardContent>
      </Card>

      <div className="flex justify-end space-x-4">
        <Button variant="outline">
          Reset to Defaults
        </Button>
        <Button onClick={handleSaveSettings} className="px-8">
          Save All Settings
        </Button>
      </div>
    </div>
  );
};

export default AppSettings;
