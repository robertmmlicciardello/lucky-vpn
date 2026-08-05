
import { useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Switch } from "@/components/ui/switch";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { 
  CreditCard, 
  Plus, 
  Edit, 
  Trash2, 
  Check, 
  X, 
  Clock,
  AlertCircle,
  DollarSign,
  Smartphone
} from "lucide-react";
import { useToast } from "@/hooks/use-toast";

interface PaymentAccount {
  id: string;
  name: string;
  type: string;
  accountNumber: string;
  accountName: string;
  isActive: boolean;
  instructions: string;
}

interface Transaction {
  id: string;
  userId: string;
  userName: string;
  amount: number;
  paymentMethod: string;
  transactionId: string;
  status: 'pending' | 'approved' | 'rejected';
  submittedAt: string;
  plan: string;
}

const PaymentManagement = () => {
  const { toast } = useToast();
  const [paymentAccounts, setPaymentAccounts] = useState<PaymentAccount[]>([
    {
      id: "1",
      name: "KPay",
      type: "Mobile Payment",
      accountNumber: "09123456789",
      accountName: "VPN Service",
      isActive: true,
      instructions: "Transfer to this KPay number and send screenshot with transaction ID"
    },
    {
      id: "2",
      name: "Wave Money",
      type: "Mobile Payment",
      accountNumber: "09987654321",
      accountName: "VPN Service",
      isActive: true,
      instructions: "Send money via Wave and provide transaction reference"
    }
  ]);

  const [transactions, setTransactions] = useState<Transaction[]>([
    {
      id: "1",
      userId: "user1",
      userName: "John Doe",
      amount: 9.99,
      paymentMethod: "KPay",
      transactionId: "KP123456789",
      status: "pending",
      submittedAt: "2024-01-15 10:30:00",
      plan: "Premium Monthly"
    },
    {
      id: "2",
      userId: "user2",
      userName: "Jane Smith",
      amount: 19.99,
      paymentMethod: "Wave Money",
      transactionId: "WV987654321",
      status: "approved",
      submittedAt: "2024-01-14 15:45:00",
      plan: "Premium Yearly"
    }
  ]);

  const [newAccount, setNewAccount] = useState({
    name: "",
    type: "",
    accountNumber: "",
    accountName: "",
    instructions: ""
  });

  const [editingAccount, setEditingAccount] = useState<string | null>(null);

  const handleAddAccount = () => {
    if (!newAccount.name || !newAccount.accountNumber || !newAccount.accountName) {
      toast({
        title: "Error",
        description: "Please fill in all required fields",
        variant: "destructive",
      });
      return;
    }

    const account: PaymentAccount = {
      id: Date.now().toString(),
      ...newAccount,
      isActive: true
    };

    setPaymentAccounts([...paymentAccounts, account]);
    setNewAccount({
      name: "",
      type: "",
      accountNumber: "",
      accountName: "",
      instructions: ""
    });

    toast({
      title: "Success",
      description: "Payment account added successfully",
    });
  };

  const handleToggleAccount = (id: string) => {
    setPaymentAccounts(accounts =>
      accounts.map(account =>
        account.id === id ? { ...account, isActive: !account.isActive } : account
      )
    );
  };

  const handleDeleteAccount = (id: string) => {
    setPaymentAccounts(accounts => accounts.filter(account => account.id !== id));
    toast({
      title: "Success",
      description: "Payment account deleted successfully",
    });
  };

  const handleTransactionAction = (id: string, action: 'approve' | 'reject') => {
    setTransactions(transactions =>
      transactions.map(transaction =>
        transaction.id === id ? { ...transaction, status: action === 'approve' ? 'approved' : 'rejected' } : transaction
      )
    );

    toast({
      title: "Success",
      description: `Transaction ${action}d successfully`,
    });
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'pending':
        return 'bg-yellow-100 text-yellow-800';
      case 'approved':
        return 'bg-green-100 text-green-800';
      case 'rejected':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'pending':
        return <Clock className="w-4 h-4" />;
      case 'approved':
        return <Check className="w-4 h-4" />;
      case 'rejected':
        return <X className="w-4 h-4" />;
      default:
        return <AlertCircle className="w-4 h-4" />;
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold text-gray-900">Payment Management</h2>
          <p className="text-gray-600 mt-2">Manage payment accounts and approve transactions</p>
        </div>
      </div>

      <Tabs defaultValue="accounts" className="space-y-6">
        <TabsList className="grid w-full grid-cols-2">
          <TabsTrigger value="accounts">Payment Accounts</TabsTrigger>
          <TabsTrigger value="transactions">Transactions</TabsTrigger>
        </TabsList>

        <TabsContent value="accounts" className="space-y-6">
          {/* Add New Account */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Plus className="w-5 h-5" />
                Add Payment Account
              </CardTitle>
              <CardDescription>
                Add new payment gateway accounts for users to make payments
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="name">Payment Gateway Name *</Label>
                  <Input
                    id="name"
                    placeholder="e.g., KPay, Wave Money, AYA Pay"
                    value={newAccount.name}
                    onChange={(e) => setNewAccount({ ...newAccount, name: e.target.value })}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="type">Type</Label>
                  <Select value={newAccount.type} onValueChange={(value) => setNewAccount({ ...newAccount, type: value })}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select payment type" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="Mobile Payment">Mobile Payment</SelectItem>
                      <SelectItem value="Bank Account">Bank Account</SelectItem>
                      <SelectItem value="Digital Wallet">Digital Wallet</SelectItem>
                      <SelectItem value="Other">Other</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="accountNumber">Account Number/Phone *</Label>
                  <Input
                    id="accountNumber"
                    placeholder="09xxxxxxxxx or account number"
                    value={newAccount.accountNumber}
                    onChange={(e) => setNewAccount({ ...newAccount, accountNumber: e.target.value })}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="accountName">Account Name *</Label>
                  <Input
                    id="accountName"
                    placeholder="Account holder name"
                    value={newAccount.accountName}
                    onChange={(e) => setNewAccount({ ...newAccount, accountName: e.target.value })}
                  />
                </div>
              </div>
              <div className="space-y-2">
                <Label htmlFor="instructions">Payment Instructions</Label>
                <Textarea
                  id="instructions"
                  placeholder="Instructions for users on how to make payment..."
                  value={newAccount.instructions}
                  onChange={(e) => setNewAccount({ ...newAccount, instructions: e.target.value })}
                />
              </div>
              <Button onClick={handleAddAccount} className="w-full">
                <Plus className="w-4 h-4 mr-2" />
                Add Payment Account
              </Button>
            </CardContent>
          </Card>

          {/* Payment Accounts List */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {paymentAccounts.map((account) => (
              <Card key={account.id} className="relative">
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle className="flex items-center gap-2">
                      <Smartphone className="w-5 h-5" />
                      {account.name}
                    </CardTitle>
                    <div className="flex items-center gap-2">
                      <Switch
                        checked={account.isActive}
                        onCheckedChange={() => handleToggleAccount(account.id)}
                      />
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => handleDeleteAccount(account.id)}
                        className="text-red-600 hover:text-red-700"
                      >
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                  <Badge variant={account.isActive ? "default" : "secondary"}>
                    {account.isActive ? "Active" : "Inactive"}
                  </Badge>
                </CardHeader>
                <CardContent className="space-y-3">
                  <div>
                    <p className="text-sm font-medium text-gray-600">Type:</p>
                    <p className="text-sm">{account.type || "Not specified"}</p>
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-600">Account Number:</p>
                    <p className="text-sm font-mono">{account.accountNumber}</p>
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-600">Account Name:</p>
                    <p className="text-sm">{account.accountName}</p>
                  </div>
                  {account.instructions && (
                    <div>
                      <p className="text-sm font-medium text-gray-600">Instructions:</p>
                      <p className="text-sm text-gray-700">{account.instructions}</p>
                    </div>
                  )}
                </CardContent>
              </Card>
            ))}
          </div>
        </TabsContent>

        <TabsContent value="transactions" className="space-y-6">
          {/* Transaction Statistics */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
            <Card>
              <CardContent className="p-6">
                <div className="flex items-center gap-2">
                  <Clock className="w-5 h-5 text-yellow-600" />
                  <div>
                    <p className="text-2xl font-bold text-yellow-600">
                      {transactions.filter(t => t.status === 'pending').length}
                    </p>
                    <p className="text-sm text-gray-600">Pending</p>
                  </div>
                </div>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="p-6">
                <div className="flex items-center gap-2">
                  <Check className="w-5 h-5 text-green-600" />
                  <div>
                    <p className="text-2xl font-bold text-green-600">
                      {transactions.filter(t => t.status === 'approved').length}
                    </p>
                    <p className="text-sm text-gray-600">Approved</p>
                  </div>
                </div>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="p-6">
                <div className="flex items-center gap-2">
                  <X className="w-5 h-5 text-red-600" />
                  <div>
                    <p className="text-2xl font-bold text-red-600">
                      {transactions.filter(t => t.status === 'rejected').length}
                    </p>
                    <p className="text-sm text-gray-600">Rejected</p>
                  </div>
                </div>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="p-6">
                <div className="flex items-center gap-2">
                  <DollarSign className="w-5 h-5 text-blue-600" />
                  <div>
                    <p className="text-2xl font-bold text-blue-600">
                      ${transactions.filter(t => t.status === 'approved').reduce((sum, t) => sum + t.amount, 0).toFixed(2)}
                    </p>
                    <p className="text-sm text-gray-600">Total Revenue</p>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Transactions List */}
          <Card>
            <CardHeader>
              <CardTitle>Recent Transactions</CardTitle>
              <CardDescription>Review and approve user payment submissions</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {transactions.map((transaction) => (
                  <div key={transaction.id} className="border rounded-lg p-4 space-y-3">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <div className="flex items-center gap-2">
                          {getStatusIcon(transaction.status)}
                          <Badge className={getStatusColor(transaction.status)}>
                            {transaction.status.charAt(0).toUpperCase() + transaction.status.slice(1)}
                          </Badge>
                        </div>
                        <div>
                          <p className="font-medium">{transaction.userName}</p>
                          <p className="text-sm text-gray-600">User ID: {transaction.userId}</p>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className="font-bold text-lg">${transaction.amount}</p>
                        <p className="text-sm text-gray-600">{transaction.plan}</p>
                      </div>
                    </div>
                    
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                      <div>
                        <p className="font-medium text-gray-600">Payment Method:</p>
                        <p>{transaction.paymentMethod}</p>
                      </div>
                      <div>
                        <p className="font-medium text-gray-600">Transaction ID:</p>
                        <p className="font-mono">{transaction.transactionId}</p>
                      </div>
                      <div>
                        <p className="font-medium text-gray-600">Submitted:</p>
                        <p>{transaction.submittedAt}</p>
                      </div>
                    </div>

                    {transaction.status === 'pending' && (
                      <div className="flex gap-2 pt-2">
                        <Button
                          size="sm"
                          onClick={() => handleTransactionAction(transaction.id, 'approve')}
                          className="bg-green-600 hover:bg-green-700"
                        >
                          <Check className="w-4 h-4 mr-1" />
                          Approve
                        </Button>
                        <Button
                          size="sm"
                          variant="destructive"
                          onClick={() => handleTransactionAction(transaction.id, 'reject')}
                        >
                          <X className="w-4 h-4 mr-1" />
                          Reject
                        </Button>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default PaymentManagement;
