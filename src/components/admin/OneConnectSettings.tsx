
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Badge } from "@/components/ui/badge";
import { Settings, Key, CheckCircle, XCircle, RefreshCw } from "lucide-react";
import { useState } from "react";
import { useToast } from "@/hooks/use-toast";

const OneConnectSettings = () => {
  const { toast } = useToast();
  const [oneConnectConfig, setOneConnectConfig] = useState({
    apiKey: "",
    apiUrl: "https://api.oneconnect.com/v1",
    isEnabled: false,
    isConnected: false,
    lastSync: null as string | null
  });
  
  const [isLoading, setIsLoading] = useState(false);

  const handleSaveConfig = async () => {
    if (!oneConnectConfig.apiKey) {
      toast({
        title: "Error",
        description: "Please enter your OneConnect API key",
        variant: "destructive"
      });
      return;
    }

    setIsLoading(true);
    
    try {
      // Simulate API call to save and test configuration
      setTimeout(() => {
        setOneConnectConfig(prev => ({
          ...prev,
          isConnected: true,
          lastSync: new Date().toISOString()
        }));
        
        toast({
          title: "Success",
          description: "OneConnect API configured successfully"
        });
        setIsLoading(false);
      }, 2000);
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to connect to OneConnect API",
        variant: "destructive"
      });
      setIsLoading(false);
    }
  };

  const handleToggleOneConnect = (enabled: boolean) => {
    setOneConnectConfig(prev => ({ ...prev, isEnabled: enabled }));
    
    if (enabled && !oneConnectConfig.isConnected) {
      toast({
        title: "Info",
        description: "Please configure your API key first",
        variant: "default"
      });
    } else {
      toast({
        title: enabled ? "Enabled" : "Disabled",
        description: `OneConnect VPN ${enabled ? 'enabled' : 'disabled'} successfully`
      });
    }
  };

  const syncServers = async () => {
    if (!oneConnectConfig.isConnected) {
      toast({
        title: "Error",
        description: "Please configure OneConnect API first",
        variant: "destructive"
      });
      return;
    }

    setIsLoading(true);
    
    try {
      // Simulate server sync
      setTimeout(() => {
        setOneConnectConfig(prev => ({
          ...prev,
          lastSync: new Date().toISOString()
        }));
        
        toast({
          title: "Success",
          description: "Servers synchronized successfully"
        });
        setIsLoading(false);
      }, 3000);
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to sync servers",
        variant: "destructive"
      });
      setIsLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* OneConnect Configuration */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center">
                <Key className="w-5 h-5 text-purple-600" />
              </div>
              <div>
                <CardTitle>OneConnect API Configuration</CardTitle>
                <CardDescription>
                  Configure your OneConnect VPN API credentials
                </CardDescription>
              </div>
            </div>
            <div className="flex items-center space-x-2">
              {oneConnectConfig.isConnected ? (
                <Badge variant="default" className="bg-green-500">
                  <CheckCircle className="w-3 h-3 mr-1" />
                  Connected
                </Badge>
              ) : (
                <Badge variant="secondary">
                  <XCircle className="w-3 h-3 mr-1" />
                  Not Connected
                </Badge>
              )}
            </div>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div>
            <Label htmlFor="apiUrl">API Endpoint URL</Label>
            <Input
              id="apiUrl"
              value={oneConnectConfig.apiUrl}
              onChange={(e) => setOneConnectConfig(prev => ({
                ...prev,
                apiUrl: e.target.value
              }))}
              placeholder="https://api.oneconnect.com/v1"
            />
          </div>
          
          <div>
            <Label htmlFor="apiKey">OneConnect API Key</Label>
            <Input
              id="apiKey"
              type="password"
              value={oneConnectConfig.apiKey}
              onChange={(e) => setOneConnectConfig(prev => ({
                ...prev,
                apiKey: e.target.value
              }))}
              placeholder="Enter your OneConnect API key"
            />
            <p className="text-sm text-gray-500 mt-1">
              Get your API key from OneConnect dashboard
            </p>
          </div>

          <div className="flex justify-end">
            <Button 
              onClick={handleSaveConfig}
              disabled={isLoading}
              className="flex items-center gap-2"
            >
              {isLoading ? (
                <RefreshCw className="w-4 h-4 animate-spin" />
              ) : (
                <Settings className="w-4 h-4" />
              )}
              {isLoading ? "Testing Connection..." : "Save & Test"}
            </Button>
          </div>

          {oneConnectConfig.lastSync && (
            <div className="text-sm text-gray-500 border-t pt-3">
              Last synchronized: {new Date(oneConnectConfig.lastSync).toLocaleString()}
            </div>
          )}
        </CardContent>
      </Card>

      {/* OneConnect Enable/Disable */}
      <Card>
        <CardHeader>
          <CardTitle>OneConnect Service Control</CardTitle>
          <CardDescription>
            Enable or disable OneConnect VPN integration for your app
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h4 className="font-medium">OneConnect VPN Service</h4>
              <p className="text-sm text-gray-500">
                {oneConnectConfig.isEnabled 
                  ? "Users can access OneConnect servers" 
                  : "OneConnect servers are disabled for users"
                }
              </p>
            </div>
            <Switch
              checked={oneConnectConfig.isEnabled}
              onCheckedChange={handleToggleOneConnect}
              disabled={!oneConnectConfig.isConnected}
            />
          </div>

          {oneConnectConfig.isEnabled && oneConnectConfig.isConnected && (
            <div className="border-t pt-4">
              <div className="flex justify-between items-center">
                <div>
                  <h4 className="font-medium">Server Synchronization</h4>
                  <p className="text-sm text-gray-500">
                    Sync latest servers from OneConnect API
                  </p>
                </div>
                <Button 
                  variant="outline" 
                  onClick={syncServers}
                  disabled={isLoading}
                  className="flex items-center gap-2"
                >
                  <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
                  Sync Servers
                </Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default OneConnectSettings;
