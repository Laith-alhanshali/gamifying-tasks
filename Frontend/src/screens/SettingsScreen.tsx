import React from 'react';
import { User } from '../types';
import { Card } from '../components/Card';
import { Button } from '../components/Button';
import { Settings as SettingsIcon, Bell, Moon, Sun, Palette, User as UserIcon } from 'lucide-react';

interface SettingsScreenProps {
  user: User;
  darkMode: boolean;
  onToggleDarkMode: () => void;
}

export const SettingsScreen: React.FC<SettingsScreenProps> = ({ user, darkMode, onToggleDarkMode }) => {
  return (
    <div className="space-y-6 max-w-4xl">
      <div>
        <h1>Settings</h1>
        <p className="text-[rgb(var(--color-text-secondary))] mt-1">
          Customize your LevelUp Tasks experience
        </p>
      </div>

      {/* Profile Settings */}
      <Card className="p-6">
        <div className="flex items-center gap-3 mb-6">
          <UserIcon size={20} className="text-violet-400" />
          <h3>Profile Settings</h3>
        </div>
        <div className="space-y-4">
          <div>
            <label className="block text-sm mb-2 text-[rgb(var(--color-text-secondary))]">Username</label>
            <input
              type="text"
              value={user.username}
              className="w-full px-4 py-2 bg-[rgb(var(--color-surface-elevated))] border border-[rgb(var(--color-border))] rounded-lg text-[rgb(var(--color-text-primary))] focus:outline-none focus:ring-2 focus:ring-violet-500"
            />
          </div>
          <div>
            <label className="block text-sm mb-2 text-[rgb(var(--color-text-secondary))]">Email</label>
            <input
              type="email"
              placeholder="your.email@example.com"
              className="w-full px-4 py-2 bg-[rgb(var(--color-surface-elevated))] border border-[rgb(var(--color-border))] rounded-lg text-[rgb(var(--color-text-primary))] focus:outline-none focus:ring-2 focus:ring-violet-500"
            />
          </div>
          <Button>Save Profile</Button>
        </div>
      </Card>

      {/* Appearance */}
      <Card className="p-6">
        <div className="flex items-center gap-3 mb-6">
          <Palette size={20} className="text-violet-400" />
          <h3>Appearance</h3>
        </div>
        <div className="space-y-4">
          <div className="flex items-center justify-between p-4 bg-[rgb(var(--color-surface-elevated))] rounded-lg">
            <div className="flex items-center gap-3">
              {darkMode ? <Moon size={20} /> : <Sun size={20} />}
              <div>
                <h4 className="text-sm">Dark Mode</h4>
                <p className="text-xs text-[rgb(var(--color-text-tertiary))]">
                  {darkMode ? 'Dark theme is enabled' : 'Light theme is enabled'}
                </p>
              </div>
            </div>
            <button
              onClick={onToggleDarkMode}
              className={`relative w-14 h-8 rounded-full transition-colors ${
                darkMode ? 'bg-violet-500' : 'bg-[rgb(var(--color-border))]'
              }`}
            >
              <div
                className={`absolute top-1 w-6 h-6 bg-white rounded-full transition-transform ${
                  darkMode ? 'translate-x-7' : 'translate-x-1'
                }`}
              />
            </button>
          </div>
        </div>
      </Card>

      {/* Notifications */}
      <Card className="p-6">
        <div className="flex items-center gap-3 mb-6">
          <Bell size={20} className="text-violet-400" />
          <h3>Notifications</h3>
        </div>
        <div className="space-y-4">
          {[
            { id: 'task-reminders', label: 'Task Reminders', description: 'Get notified about upcoming tasks' },
            { id: 'achievements', label: 'Achievement Unlocks', description: 'Celebrate when you unlock achievements' },
            { id: 'quests', label: 'Quest Progress', description: 'Updates on quest completion' },
            { id: 'streak', label: 'Streak Alerts', description: 'Daily reminders to maintain your streak' },
          ].map((setting) => (
            <div key={setting.id} className="flex items-center justify-between p-4 bg-[rgb(var(--color-surface-elevated))] rounded-lg">
              <div>
                <h4 className="text-sm">{setting.label}</h4>
                <p className="text-xs text-[rgb(var(--color-text-tertiary))]">{setting.description}</p>
              </div>
              <button className="relative w-14 h-8 rounded-full bg-violet-500">
                <div className="absolute top-1 translate-x-7 w-6 h-6 bg-white rounded-full" />
              </button>
            </div>
          ))}
        </div>
      </Card>

      {/* Privacy & Data */}
      <Card className="p-6">
        <div className="flex items-center gap-3 mb-6">
          <SettingsIcon size={20} className="text-violet-400" />
          <h3>Privacy & Data</h3>
        </div>
        <div className="space-y-4">
          <div className="flex items-center justify-between p-4 bg-[rgb(var(--color-surface-elevated))] rounded-lg">
            <div>
              <h4 className="text-sm">Show on Leaderboard</h4>
              <p className="text-xs text-[rgb(var(--color-text-tertiary))]">Display your profile in public rankings</p>
            </div>
            <button className="relative w-14 h-8 rounded-full bg-violet-500">
              <div className="absolute top-1 translate-x-7 w-6 h-6 bg-white rounded-full" />
            </button>
          </div>
          <Button variant="secondary" className="w-full">Export My Data</Button>
          <Button variant="danger" className="w-full">Delete Account</Button>
        </div>
      </Card>

      {/* About */}
      <Card className="p-6 bg-gradient-to-br from-violet-500/10 to-blue-500/10 border-violet-500/20">
        <h3 className="mb-3">About LevelUp Tasks</h3>
        <div className="space-y-2 text-sm text-[rgb(var(--color-text-secondary))]">
          <p>Version 1.0.0</p>
          <p>A gamified task manager designed to boost your productivity through game mechanics and achievement systems.</p>
          <p className="text-xs mt-4">Built with React, TypeScript, and Tailwind CSS</p>
        </div>
      </Card>
    </div>
  );
};
