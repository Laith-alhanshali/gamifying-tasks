import React from 'react';
import {
  LayoutDashboard,
  CheckSquare,
  Calendar,
  Target,
  Award,
  Trophy,
  Gift,
  Settings,
  Shield,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';
import { User } from '../types';

interface SidebarProps {
  currentScreen: string;
  onNavigate: (screen: string) => void;
  user: User;
  collapsed: boolean;
  onToggleCollapse: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  currentScreen,
  onNavigate,
  user,
  collapsed,
  onToggleCollapse,
}) => {
  const menuItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'tasks', label: 'Tasks', icon: CheckSquare },
    { id: 'calendar', label: 'Calendar', icon: Calendar },
    { id: 'quests', label: 'Quests', icon: Target },
    { id: 'achievements', label: 'Achievements', icon: Award },
    { id: 'leaderboard', label: 'Leaderboard', icon: Trophy },
    { id: 'shop', label: 'Mystery Shop', icon: Gift },
  ];

  if (user.role === 'admin') {
    menuItems.push({ id: 'admin', label: 'Admin Panel', icon: Shield });
  }

  menuItems.push({ id: 'settings', label: 'Settings', icon: Settings });

  return (
    <aside
  style={{ top: 64, height: "calc(100vh - 64px)" }} // 64px = h-16
  className={`fixed left-0 bg-[rgb(var(--color-surface))] border-r border-[rgb(var(--color-border))] transition-all duration-300 z-40 ${
    collapsed ? "w-20" : "w-64"
  }`}
>

      <div className="flex flex-col h-full">
        {/* Logo */}
        <div className="p-6 border-b border-[rgb(var(--color-border))] flex items-center justify-between">
          {!collapsed && (
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-gradient-to-br from-violet-500 to-blue-500 rounded-xl flex items-center justify-center shadow-lg shadow-violet-500/50 relative overflow-hidden">
                <Trophy className="text-white relative z-10" size={22} />
                <div className="absolute inset-0 bg-gradient-to-tr from-transparent via-white/30 to-transparent" />
              </div>
              <div>
                <h2 className="bg-gradient-to-r from-violet-400 to-blue-400 bg-clip-text text-transparent font-bold">
                  TaskUp
                </h2>
                <p className="text-xs text-[rgb(var(--color-text-tertiary))] font-medium">Tasks</p>
              </div>
            </div>
          )}
          {collapsed && (
            <div className="w-10 h-10 bg-gradient-to-br from-violet-500 to-blue-500 rounded-xl flex items-center justify-center mx-auto shadow-lg shadow-violet-500/50 relative overflow-hidden">
              <Trophy className="text-white relative z-10" size={22} />
              <div className="absolute inset-0 bg-gradient-to-tr from-transparent via-white/30 to-transparent" />
            </div>
          )}
        </div>

        {/* Navigation */}
        <nav className="flex-1 p-4 space-y-2 overflow-y-auto">
          {menuItems.map((item) => {
            const Icon = item.icon;
            const isActive = currentScreen === item.id;

            return (
              <button
                key={item.id}
                onClick={() => onNavigate(item.id)}
                className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all ${
                  isActive
                    ? 'gradient-primary text-white shadow-lg shadow-violet-500/30'
                    : 'text-[rgb(var(--color-text-secondary))] hover:bg-[rgb(var(--color-surface-elevated))]'
                } ${collapsed ? 'justify-center' : ''}`}
                title={collapsed ? item.label : ''}
              >
                <Icon size={20} />
                {!collapsed && <span>{item.label}</span>}
              </button>
            );
          })}
        </nav>

        {/* Collapse Toggle */}
        <div className="p-4 border-t border-[rgb(var(--color-border))]">
          <button
            onClick={onToggleCollapse}
            className="w-full flex items-center justify-center gap-2 px-3 py-2 rounded-lg text-[rgb(var(--color-text-secondary))] hover:bg-[rgb(var(--color-surface-elevated))] transition-colors"
          >
            {collapsed ? <ChevronRight size={20} /> : <ChevronLeft size={20} />}
            {!collapsed && <span className="text-sm">Collapse</span>}
          </button>
        </div>
      </div>
    </aside>
  );
};