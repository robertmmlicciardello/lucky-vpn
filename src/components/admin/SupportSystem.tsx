
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { MessageSquare, Send, Clock, CheckCircle, AlertTriangle, Search } from "lucide-react";
import { useState } from "react";
import { useToast } from "@/hooks/use-toast";

interface SupportTicket {
  id: number;
  subject: string;
  message: string;
  userEmail: string;
  userName: string;
  status: 'open' | 'in-progress' | 'resolved' | 'closed';
  priority: 'low' | 'medium' | 'high' | 'urgent';
  createdAt: string;
  updatedAt: string;
  adminReply?: string;
}

const SupportSystem = () => {
  const { toast } = useToast();
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedTicket, setSelectedTicket] = useState<SupportTicket | null>(null);
  const [replyMessage, setReplyMessage] = useState('');
  const [tickets, setTickets] = useState<SupportTicket[]>([
    {
      id: 1,
      subject: "Connection Issues with US Server",
      message: "I'm having trouble connecting to US servers. The app keeps showing connection timeout errors.",
      userEmail: "john@example.com",
      userName: "John Doe",
      status: 'open',
      priority: 'high',
      createdAt: '2024-06-12 09:30',
      updatedAt: '2024-06-12 09:30'
    },
    {
      id: 2,
      subject: "Premium Subscription Not Working",
      message: "I purchased premium subscription but I still can't access premium servers.",
      userEmail: "jane@example.com", 
      userName: "Jane Smith",
      status: 'in-progress',
      priority: 'urgent',
      createdAt: '2024-06-11 14:20',
      updatedAt: '2024-06-11 16:45',
      adminReply: "We're looking into your subscription status. Please check your email for updates."
    },
    {
      id: 3,
      subject: "App Crashes on Android 14",
      message: "The app keeps crashing when I try to connect to any server on my Android 14 device.",
      userEmail: "mike@example.com",
      userName: "Mike Johnson", 
      status: 'resolved',
      priority: 'medium',
      createdAt: '2024-06-10 11:15',
      updatedAt: '2024-06-11 10:30',
      adminReply: "This issue has been fixed in the latest app update. Please update your app from Play Store."
    }
  ]);

  const filteredTickets = tickets.filter(ticket =>
    ticket.subject.toLowerCase().includes(searchTerm.toLowerCase()) ||
    ticket.userEmail.toLowerCase().includes(searchTerm.toLowerCase()) ||
    ticket.userName.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleReply = () => {
    if (!selectedTicket || !replyMessage.trim()) {
      toast({
        title: "Error",
        description: "Please enter a reply message",
        variant: "destructive"
      });
      return;
    }

    setTickets(tickets.map(ticket => 
      ticket.id === selectedTicket.id 
        ? { 
            ...ticket, 
            adminReply: replyMessage,
            status: 'in-progress',
            updatedAt: new Date().toLocaleString()
          }
        : ticket
    ));

    setReplyMessage('');
    setSelectedTicket(null);
    
    toast({
      title: "Success",
      description: "Reply sent successfully"
    });
  };

  const handleStatusChange = (ticketId: number, newStatus: SupportTicket['status']) => {
    setTickets(tickets.map(ticket => 
      ticket.id === ticketId 
        ? { ...ticket, status: newStatus, updatedAt: new Date().toLocaleString() }
        : ticket
    ));
    
    toast({
      title: "Success",
      description: "Ticket status updated"
    });
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'open': return 'bg-blue-500';
      case 'in-progress': return 'bg-yellow-500';
      case 'resolved': return 'bg-green-500';
      case 'closed': return 'bg-gray-500';
      default: return 'bg-gray-500';
    }
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'low': return 'text-green-600 bg-green-50';
      case 'medium': return 'text-yellow-600 bg-yellow-50';
      case 'high': return 'text-orange-600 bg-orange-50';
      case 'urgent': return 'text-red-600 bg-red-50';
      default: return 'text-gray-600 bg-gray-50';
    }
  };

  const getPriorityIcon = (priority: string) => {
    switch (priority) {
      case 'urgent': return <AlertTriangle className="w-4 h-4" />;
      case 'high': return <Clock className="w-4 h-4" />;
      default: return <MessageSquare className="w-4 h-4" />;
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Support System</h2>
          <p className="text-gray-600">Manage user support tickets and inquiries</p>
        </div>
      </div>

      {/* Search and Stats */}
      <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
        <Card className="md:col-span-2">
          <CardContent className="p-4">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
              <Input
                placeholder="Search tickets..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10"
              />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-4 text-center">
            <div className="text-2xl font-bold text-blue-600">{tickets.filter(t => t.status === 'open').length}</div>
            <div className="text-sm text-gray-600">Open</div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-4 text-center">
            <div className="text-2xl font-bold text-yellow-600">{tickets.filter(t => t.status === 'in-progress').length}</div>
            <div className="text-sm text-gray-600">In Progress</div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-4 text-center">
            <div className="text-2xl font-bold text-green-600">{tickets.filter(t => t.status === 'resolved').length}</div>
            <div className="text-sm text-gray-600">Resolved</div>
          </CardContent>
        </Card>
      </div>

      {/* Tickets List */}
      <Card>
        <CardHeader>
          <CardTitle>Support Tickets</CardTitle>
          <CardDescription>Manage and respond to user support requests</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {filteredTickets.map((ticket) => (
              <div key={ticket.id} className="border rounded-lg p-4 hover:bg-gray-50">
                <div className="flex items-start justify-between">
                  <div className="flex items-start space-x-4 flex-1">
                    <Avatar>
                      <AvatarImage src={`https://api.dicebear.com/6/initials/svg?seed=${ticket.userName}`} />
                      <AvatarFallback>{ticket.userName.charAt(0)}</AvatarFallback>
                    </Avatar>
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-2">
                        <h3 className="font-semibold text-gray-900">{ticket.subject}</h3>
                        <Badge className={getPriorityColor(ticket.priority)}>
                          <div className="flex items-center gap-1">
                            {getPriorityIcon(ticket.priority)}
                            {ticket.priority}
                          </div>
                        </Badge>
                      </div>
                      <p className="text-gray-600 text-sm mb-2">{ticket.message}</p>
                      <div className="flex items-center gap-4 text-xs text-gray-500">
                        <span>From: {ticket.userName} ({ticket.userEmail})</span>
                        <span>Created: {ticket.createdAt}</span>
                        <span>Updated: {ticket.updatedAt}</span>
                      </div>
                      {ticket.adminReply && (
                        <div className="mt-3 p-3 bg-blue-50 rounded border-l-4 border-blue-400">
                          <p className="text-sm text-blue-800">
                            <strong>Admin Reply:</strong> {ticket.adminReply}
                          </p>
                        </div>
                      )}
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className={`w-3 h-3 rounded-full ${getStatusColor(ticket.status)}`}></div>
                    <Select 
                      value={ticket.status} 
                      onValueChange={(value: SupportTicket['status']) => handleStatusChange(ticket.id, value)}
                    >
                      <SelectTrigger className="w-32">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="open">Open</SelectItem>
                        <SelectItem value="in-progress">In Progress</SelectItem>
                        <SelectItem value="resolved">Resolved</SelectItem>
                        <SelectItem value="closed">Closed</SelectItem>
                      </SelectContent>
                    </Select>
                    <Button 
                      size="sm" 
                      onClick={() => setSelectedTicket(ticket)}
                    >
                      <MessageSquare className="w-4 h-4 mr-1" />
                      Reply
                    </Button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Reply Modal/Form */}
      {selectedTicket && (
        <Card>
          <CardHeader>
            <CardTitle>Reply to: {selectedTicket.subject}</CardTitle>
            <CardDescription>Send reply to {selectedTicket.userName} ({selectedTicket.userEmail})</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="p-3 bg-gray-50 rounded">
              <p className="text-sm text-gray-700">
                <strong>Original Message:</strong> {selectedTicket.message}
              </p>
            </div>
            <Textarea
              placeholder="Type your reply..."
              value={replyMessage}
              onChange={(e) => setReplyMessage(e.target.value)}
              rows={4}
            />
            <div className="flex gap-2">
              <Button onClick={handleReply}>
                <Send className="w-4 h-4 mr-2" />
                Send Reply
              </Button>
              <Button variant="outline" onClick={() => setSelectedTicket(null)}>
                Cancel
              </Button>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
};

export default SupportSystem;
