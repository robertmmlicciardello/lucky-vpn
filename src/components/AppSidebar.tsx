import type { LucideIcon } from "lucide-react";
import { useTranslation } from "react-i18next";
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from "@/components/ui/sidebar";
import {
  LayoutDashboard,
  Server,
  Users,
  DollarSign,
  Gift,
  Bell,
  HeadphonesIcon,
  FileText,
  Trophy,
  CreditCard,
  Settings,
  Megaphone,
} from "lucide-react";

interface AppSidebarProps {
  activeTab: string;
  setActiveTab: (tab: string) => void;
}

interface MenuItem {
  title: string;
  icon: LucideIcon;
  key: string;
}

const AppSidebar = ({ activeTab, setActiveTab }: AppSidebarProps) => {
  const { t } = useTranslation();

  const menuItems = [
    {
      title: t("nav.dashboard"),
      icon: LayoutDashboard,
      key: "dashboard",
    },
    {
      title: t("nav.servers"),
      icon: Server,
      key: "servers",
    },
    {
      title: t("nav.users"),
      icon: Users,
      key: "users",
    },
    {
      title: t("nav.ads"),
      icon: Megaphone,
      key: "ads",
    },
    {
      title: "Multi Ad Networks",
      icon: DollarSign,
      key: "multi-ads",
    },
    {
      title: t("nav.plans"),
      icon: CreditCard,
      key: "plans",
    },
    {
      title: t("nav.rewards"),
      icon: Gift,
      key: "rewards",
    },
    {
      title: t("nav.notifications"),
      icon: Bell,
      key: "notifications",
    },
    {
      title: t("nav.support"),
      icon: HeadphonesIcon,
      key: "support",
    },
    {
      title: t("nav.blog"),
      icon: FileText,
      key: "blog",
    },
    {
      title: t("nav.leaderboard"),
      icon: Trophy,
      key: "leaderboard",
    },
    {
      title: t("nav.payments"),
      icon: DollarSign,
      key: "payments",
    },
    {
      title: t("nav.settings"),
      icon: Settings,
      key: "settings",
    },
  ];

  return (
    <Sidebar>
      <SidebarContent className="bg-gradient-to-b from-blue-600 to-purple-700">
        <SidebarGroup>
          <SidebarGroupLabel className="text-white/80 font-semibold text-lg mb-4">
            Lucky VPN Master
          </SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu className="space-y-1">
              {menuItems.map((item) => (
                <SidebarMenuItem key={item.key}>
                  <SidebarMenuButton
                    onClick={() => setActiveTab(item.key)}
                    className={`w-full justify-start gap-3 px-4 py-3 rounded-lg transition-all duration-200 ${
                      activeTab === item.key
                        ? "bg-white/20 text-white shadow-lg"
                        : "text-white/70 hover:bg-white/10 hover:text-white"
                    }`}
                  >
                    <item.icon className="w-5 h-5" />
                    <span className="font-medium">{item.title}</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
    </Sidebar>
  );
};

export default AppSidebar;
