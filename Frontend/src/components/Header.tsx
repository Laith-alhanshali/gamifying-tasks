import React, { useState } from 'react';
import { Search, Bell, User as UserIcon, LogOut, Moon, Sun, Zap } from 'lucide-react';
import { User } from '../types';
import { Card } from './Card';
import { RankBadge } from './RankBadge';

interface HeaderProps {
  user: User;
  onLogout: () => void;
  darkMode: boolean;
  onToggleDarkMode: () => void;
  sidebarWidth: number; // ✅ NEW
}

export const Header: React.FC<HeaderProps> = ({
  user,
  onLogout,
  darkMode,
  onToggleDarkMode,
  sidebarWidth,
}) => {
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [showNotifications, setShowNotifications] = useState(false);

  const notifications = [
    { id: '1', text: 'Achievement unlocked: Week Warrior!', time: '5m ago', unread: true },
    { id: '2', text: 'Daily Quest completed! Claim your reward.', time: '1h ago', unread: true },
    { id: '3', text: 'You have 2 overdue tasks', time: '2h ago', unread: false },
  ];

  const unreadCount = notifications.filter((n) => n.unread).length;

  return (
    <header
      className="fixed top-0 right-0 left-0 md:left-auto h-16 bg-[rgb(var(--color-surface))] border-b border-[rgb(var(--color-border))] z-50 px-4 md:px-6 flex items-center justify-between"
      style={{
        // Mobile: full width
        left: 0,
        // Desktop: start after the *actual* sidebar width
        ...(window.innerWidth >= 768 ? { left: sidebarWidth } : {}),
      }}
    >
      {/* Search */}
      <div className="flex-1 max-w-xl hidden sm:block">
        <div className="relative">
          <Search
            className="absolute left-3 top-1/2 -translate-y-1/2 text-[rgb(var(--color-text-tertiary))]"
            size={20}
          />
          <input
            type="text"
            placeholder="Search tasks, achievements, quests..."
            className="w-full pl-10 pr-4 py-2 bg-[rgb(var(--color-surface-elevated))] border border-[rgb(var(--color-border))] rounded-lg text-[rgb(var(--color-text-primary))] placeholder:text-[rgb(var(--color-text-tertiary))] focus:outline-none focus:ring-2 focus:ring-violet-500"
          />
        </div>
      </div>

      {/* Mobile Logo */}
      <div className="sm:hidden">
        <h3 className="gradient-primary text-gradient">LevelUp</h3>
      </div>

      {/* Right section */}
      <div className="flex items-center gap-2 md:gap-4 ml-4">
        {/* User Level Badge (Desktop) */}
        <div className="hidden lg:flex items-center gap-3 px-3 py-1.5 rounded-xl bg-gradient-to-r from-violet-500/10 to-blue-500/10 border border-violet-500/20">
          <div className="level-badge w-8 h-8 rounded-lg flex items-center justify-center">
            <Zap size={16} className="text-white" fill="white" />
          </div>
          <div className="text-left">
            <p className="text-xs text-[rgb(var(--color-text-tertiary))] font-medium leading-none">Level</p>
            <p className="text-sm font-bold text-violet-400">{user.level}</p>
          </div>
        </div>

        {/* Dark Mode Toggle */}
        <button
          onClick={onToggleDarkMode}
          className="p-2 rounded-lg text-[rgb(var(--color-text-secondary))] hover:bg-[rgb(var(--color-surface-elevated))] transition-colors"
          title={darkMode ? 'Switch to light mode' : 'Switch to dark mode'}
        >
          {darkMode ? <Sun size={20} /> : <Moon size={20} />}
        </button>

        {/* Notifications */}
        <div className="relative">
          <button
            onClick={() => setShowNotifications(!showNotifications)}
            className="p-2 rounded-lg text-[rgb(var(--color-text-secondary))] hover:bg-[rgb(var(--color-surface-elevated))] transition-colors relative"
          >
            <Bell size={20} />
            {unreadCount > 0 && (
              <span className="absolute -top-1 -right-1 w-5 h-5 bg-red-500 text-white text-xs rounded-full flex items-center justify-center">
                {unreadCount}
              </span>
            )}
          </button>

          {showNotifications && (
            <div className="absolute right-0 top-12 w-80">
              <Card className="p-4 shadow-xl">
                <h3 className="mb-3">Notifications</h3>
                <div className="space-y-3">
                  {notifications.map((notification) => (
                    <div
                      key={notification.id}
                      className={`p-3 rounded-lg ${
                        notification.unread
                          ? 'bg-violet-500/10 border border-violet-500/20'
                          : 'bg-[rgb(var(--color-surface-elevated))]'
                      }`}
                    >
                      <p className="text-sm text-[rgb(var(--color-text-primary))]">{notification.text}</p>
                      <span className="text-xs text-[rgb(var(--color-text-tertiary))] mt-1 block">
                        {notification.time}
                      </span>
                    </div>
                  ))}
                </div>
              </Card>
            </div>
          )}
        </div>

        {/* User Menu */}
        <div className="relative">
          <button
            onClick={() => setShowUserMenu(!showUserMenu)}
            className="flex items-center gap-2 p-2 rounded-lg text-[rgb(var(--color-text-secondary))] hover:bg-[rgb(var(--color-surface-elevated))] transition-colors"
          >
            <div className="w-8 h-8 gradient-primary rounded-full flex items-center justify-center">
              <UserIcon size={16} className="text-white" />
            </div>
            <span className="hidden md:block text-sm">{user.username}</span>
          </button>

          {showUserMenu && (
            <div className="absolute right-0 top-12 w-64">
              <Card className="p-3 shadow-xl gaming-card">
                <div className="p-3 border-b border-[rgb(var(--color-border))]">
                  <p className="font-bold text-[rgb(var(--color-text-primary))] mb-2">{user.username}</p>
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-2">
                      <div className="level-badge w-7 h-7 rounded-lg flex items-center justify-center">
                        <Zap size={14} className="text-white" fill="white" />
                      </div>
                      <div>
                        <p className="text-xs text-[rgb(var(--color-text-tertiary))] leading-none">Level {user.level}</p>
                        <p className="text-xs font-bold text-violet-400">{user.xp} XP</p>
                      </div>
                    </div>
                    <RankBadge rank={user.rank} size="sm" showLabel={false} />
                  </div>
                  <p className="text-xs text-[rgb(var(--color-text-tertiary))]">
                    Total: {user.totalPoints.toLocaleString()} XP
                  </p>
                </div>
                <button
                  onClick={onLogout}
                  className="w-full flex items-center gap-2 px-3 py-2 mt-2 rounded-lg text-orange-400 hover:bg-orange-500/10 transition-colors text-sm font-medium"
                >
                  <LogOut size={16} />
                  Logout
                </button>
              </Card>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
