
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Bell, Send, Users, User, MessageSquare, CheckCircle } from "lucide-react";
import { useState } from "react";
import { useToast } from "@/hooks/use-toast";

interface Notification {
  id: number;
  title: string;
  message: string;
  type: 'broadcast' | 'individual';
  recipient?: string;
  status: 'sent' | 'pending' | 'failed';
  sentAt: string;
  readCount?: number;
}

const NotificationManagement = () => {
  const { toast } = useToast();
  const [notificationType, setNotificationType] = useState<'broadcast' | 'individual'>('broadcast');
  const [title, setTitle] = useState('');
  const [message, setMessage] = useState('');
  const [recipient, setRecipient] = useState('');
  const [notifications, setNotifications] = useState<Notification[]>([
    {
      id: 1,
      title: "Server Maintenance",
      message: "Scheduled maintenance on US servers from 2-4 AM",
      type: 'broadcast',
      status: 'sent',
      sentAt: '2024-06-12 10:30',
      readCount: 847
    },
    {
      id: 2,
      title: "Premium Plan Discount",
      message: "Get 50% off on premium plans this week only!",
      type: 'individual',
      recipient: 'john@example.com',
      status: 'sent',
      sentAt: '2024-06-11 15:20'
    }
  ]);

  const handleSendNotification = () => {
    if (!title.trim() || !message.trim()) {
      toast({
        title: "Error",
        description: "Please fill in all fields",
        variant: "destructive"
      });
      return;
    }

    if (notificationType === 'individual' && !recipient.trim()) {
      toast({
        title: "Error", 
        description: "Please specify recipient for individual notification",
        variant: "destructive"
      });
      return;
    }

    const newNotification: Notification = {
      id: Date.now(),
      title,
      message,
      type: notificationType,
      recipient: notificationType === 'individual' ? recipient : undefined,
      status: 'sent',
      sentAt: new Date().toLocaleString(),
      readCount: notificationType === 'broadcast' ? 0 : undefined
    };

    setNotifications([newNotification, ...notifications]);
    setTitle('');
    setMessage('');
    setRecipient('');

    toast({
      title: "Success",
      description: `${notificationType === 'broadcast' ? 'Broadcast' : 'Individual'} notification sent successfully`
    });
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'sent': return 'bg-green-500';
      case 'pending': return 'bg-yellow-500';
      case 'failed': return 'bg-red-500';
      default: return 'bg-gray-500';
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Notification Management</h2>
          <p className="text-gray-600">Send notifications to users individually or broadcast to all</p>
        </div>
      </div>

      {/* Send Notification Form */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Bell className="w-5 h-5" />
            Send New Notification
          </CardTitle>
          <CardDescription>
            Create and send notifications to your users
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium mb-2 block">Notification Type</label>
              <Select value={notificationType} onValueChange={(value: 'broadcast' | 'individual') => setNotificationType(value)}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="broadcast">
                    <div className="flex items-center gap-2">
                      <Users className="w-4 h-4" />
                      Broadcast to All Users
                    </div>
                  </SelectItem>
                  <SelectItem value="individual">
                    <div className="flex items-center gap-2">
                      <User className="w-4 h-4" />
                      Send to Individual User
                    </div>
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>
            
            {notificationType === 'individual' && (
              <div>
                <label className="text-sm font-medium mb-2 block">Recipient Email</label>
                <Input
                  placeholder="user@example.com"
                  value={recipient}
                  onChange={(e) => setRecipient(e.target.value)}
                />
              </div>
            )}
          </div>

          <div>
            <label className="text-sm font-medium mb-2 block">Title</label>
            <Input
              placeholder="Notification title..."
              value={title}
              onChange={(e) => setTitle(e.target.value)}
            />
          </div>

          <div>
            <label className="text-sm font-medium mb-2 block">Message</label>
            <Textarea
              placeholder="Notification message..."
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              rows={4}
            />
          </div>

          <Button onClick={handleSendNotification} className="w-full">
            <Send className="w-4 h-4 mr-2" />
            Send Notification
          </Button>
        </CardContent>
      </Card>

      {/* Notification History */}
      <Card>
        <CardHeader>
          <CardTitle>Notification History</CardTitle>
          <CardDescription>Recently sent notifications</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {notifications.map((notification) => (
              <div key={notification.id} className="flex items-center justify-between p-4 border rounded-lg">
                <div className="flex items-center space-x-4">
                  <div className={`w-3 h-3 rounded-full ${getStatusColor(notification.status)}`}></div>
                  <div>
                    <h3 className="font-semibold text-gray-900">{notification.title}</h3>
                    <p className="text-sm text-gray-600 mb-1">{notification.message}</p>
                    <div className="flex items-center gap-4 text-xs text-gray-500">
                      <span>Type: {notification.type}</span>
                      {notification.recipient && <span>To: {notification.recipient}</span>}
                      <span>Sent: {notification.sentAt}</span>
                      {notification.readCount !== undefined && <span>Read by: {notification.readCount} users</span>}
                    </div>
                  </div>
                </div>
                <Badge variant={notification.status === 'sent' ? 'default' : 'secondary'}>
                  {notification.status}
                </Badge>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default NotificationManagement;
