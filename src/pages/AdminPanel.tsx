
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar";
import AppSidebar from "@/components/AppSidebar";
import { Button } from "@/components/ui/button";
import { LogOut } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import LanguageSwitcher from "@/components/LanguageSwitcher";
import ServerManagement from "@/components/admin/ServerManagement";
import UserManagement from "@/components/admin/UserManagement";
import AdManagement from "@/components/admin/AdManagement";
import MultiAdNetworkManager from "@/components/admin/MultiAdNetworkManager";
import SubscriptionPlans from "@/components/admin/SubscriptionPlans";
import RewardsSystem from "@/components/admin/RewardsSystem";
import AppSettings from "@/components/admin/AppSettings";
import DashboardStats from "@/components/admin/DashboardStats";
import NotificationManagement from "@/components/admin/NotificationManagement";
import SupportSystem from "@/components/admin/SupportSystem";
import BlogManagement from "@/components/admin/BlogManagement";
import Leaderboard from "@/components/admin/Leaderboard";
import PaymentManagement from "@/components/admin/PaymentManagement";

const AdminPanel = () => {
  const { t } = useTranslation();
  const [activeTab, setActiveTab] = useState("dashboard");
  const navigate = useNavigate();
  const { toast } = useToast();

  useEffect(() => {
    const isLoggedIn = localStorage.getItem("admin_logged_in");
    if (!isLoggedIn) {
      navigate("/admin-login");
    }
  }, [navigate]);

  const handleLogout = () => {
    localStorage.removeItem("admin_logged_in");
    toast({
      title: t("common.logout"),
      description: "You have been logged out successfully",
    });
    navigate("/admin-login");
  };

  const renderContent = () => {
    switch (activeTab) {
      case "dashboard":
        return <DashboardStats />;
      case "servers":
        return <ServerManagement />;
      case "users":
        return <UserManagement />;
      case "ads":
        return <AdManagement />;
      case "multi-ads":
        return <MultiAdNetworkManager />;
      case "plans":
        return <SubscriptionPlans />;
      case "rewards":
        return <RewardsSystem />;
      case "notifications":
        return <NotificationManagement />;
      case "support":
        return <SupportSystem />;
      case "blog":
        return <BlogManagement />;
      case "leaderboard":
        return <Leaderboard />;
      case "payments":
        return <PaymentManagement />;
      case "settings":
        return <AppSettings />;
      default:
        return <DashboardStats />;
    }
  };

  return (
    <SidebarProvider>
      <div className="min-h-screen flex w-full">
        <AppSidebar activeTab={activeTab} setActiveTab={setActiveTab} />
        <main className="flex-1 overflow-hidden">
          <div className="h-full flex flex-col">
            {/* Header */}
            <header className="bg-white/70 backdrop-blur-sm border-b border-white/20 p-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                  <SidebarTrigger />
                  <h1 className="text-2xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
                    {t("admin.title")}
                  </h1>
                </div>
                <div className="flex items-center gap-2">
                  <LanguageSwitcher />
                  <Button
                    onClick={handleLogout}
                    variant="outline"
                    size="sm"
                    className="flex items-center gap-2"
                  >
                    <LogOut className="w-4 h-4" />
                    {t("common.logout")}
                  </Button>
                </div>
              </div>
            </header>

            {/* Content */}
            <div className="flex-1 overflow-auto bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-100 p-6">
              <div className="bg-white/30 backdrop-blur-sm rounded-2xl shadow-xl border border-white/20 h-full overflow-hidden">
                <div className="p-6 h-full overflow-auto">
                  {renderContent()}
                </div>
              </div>
            </div>
          </div>
        </main>
      </div>
    </SidebarProvider>
  );
};

export default AdminPanel;
