import React, { useState } from 'react';
import { User } from '../types';
import { Card } from '../components/Card';
import { Button } from '../components/Button';
import { RankBadge } from '../components/RankBadge';
import { Shield, UserPlus, Trash2, AlertTriangle, X } from 'lucide-react';

interface AdminScreenProps {
  users: User[];
  onCreateUser: (username: string, role: "admin" | "player", password: string) => Promise<void>;
  onSetRole: (userId: string, role: "admin" | "player") => Promise<void>;
  onResetStats: (userId: string) => Promise<void>;
  onDeleteUser: (userId: string) => Promise<void>;
}

export const AdminScreen: React.FC<AdminScreenProps> = ({
  users,
  onCreateUser,
  onSetRole,
  onResetStats,
  onDeleteUser,
}) => {
  const [showDeleteConfirm, setShowDeleteConfirm] = useState<string | null>(null);
  const [showCreateUser, setShowCreateUser] = useState(false);

  const [createUsername, setCreateUsername] = useState('');
  const [createRole, setCreateRole] = useState<'admin' | 'player'>('player');

  const [actionLoading, setActionLoading] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);
  
  const [createPassword, setCreatePassword] = useState("");


  const handleDeleteUser = async (userId: string) => {
    setActionError(null);
    setActionLoading(true);
    try {
      await onDeleteUser(userId);
      setShowDeleteConfirm(null);
    } catch (e: any) {
      setActionError(e?.message || 'Failed to delete user');
    } finally {
      setActionLoading(false);
    }
  };

  const handleResetStats = async (userId: string) => {
    setActionError(null);
    setActionLoading(true);
    try {
      await onResetStats(userId);
    } catch (e: any) {
      setActionError(e?.message || 'Failed to reset stats');
    } finally {
      setActionLoading(false);
    }
  };

  const handleCreateUser = async () => {
    const u = createUsername.trim();
    if (!u) {
      setActionError('Username is required');
      return;
    }

    const p = createPassword;
      if (!p.trim()) {
        setActionError("Password is required");
        return;
      }

    setActionError(null);
    setActionLoading(true);
    try {
      await onCreateUser(u, createRole, p);
      setShowCreateUser(false);
      setCreateUsername('');
      setCreateRole('player');
    } catch (e: any) {
      setActionError(e?.message || 'Failed to create user');
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <div className="flex items-center gap-2">
            <Shield size={28} className="text-violet-400" />
            <h1>Admin Panel</h1>
          </div>
          <p className="text-[rgb(var(--color-text-secondary))] mt-1">Manage users and system settings</p>
        </div>


        <Button
          onClick={() => {
            setActionError(null);
            setShowCreateUser(true);
          }}
        >
          <UserPlus size={20} />
          Create User
        </Button>
      </div>

      {/* Stats Overview */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <Card className="p-6">
          <p className="text-sm text-[rgb(var(--color-text-tertiary))] mb-1">Total Users</p>
          <h3 className="text-2xl">{users.length}</h3>
        </Card>
        <Card className="p-6">
          <p className="text-sm text-[rgb(var(--color-text-tertiary))] mb-1">Admin Users</p>
          <h3 className="text-2xl">{users.filter((u) => u.role === 'admin').length}</h3>
        </Card>
        <Card className="p-6">
          <p className="text-sm text-[rgb(var(--color-text-tertiary))] mb-1">Total Tasks Completed</p>
          <h3 className="text-2xl">{users.reduce((sum, u) => sum + u.tasksCompleted, 0)}</h3>
        </Card>
        <Card className="p-6">
          <p className="text-sm text-[rgb(var(--color-text-tertiary))] mb-1">Total XP</p>
          <h3 className="text-2xl">
            {users.reduce((sum, u) => sum + (u.totalPoints || 0), 0).toLocaleString()}
          </h3>
        </Card>
      </div>

      {actionError && (
        <Card className="p-4 border border-red-500/30 bg-red-500/10 text-red-300">{actionError}</Card>
      )}

      {/* User Management Table */}
      <Card className="p-6">
        <h3 className="mb-4">User Management</h3>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-[rgb(var(--color-border))]">
                <th className="text-left py-3 px-4 text-sm text-[rgb(var(--color-text-tertiary))]">User</th>
                <th className="text-left py-3 px-4 text-sm text-[rgb(var(--color-text-tertiary))]">Role</th>
                <th className="text-left py-3 px-4 text-sm text-[rgb(var(--color-text-tertiary))]">Level</th>
                <th className="text-left py-3 px-4 text-sm text-[rgb(var(--color-text-tertiary))]">Rank</th>
                <th className="text-left py-3 px-4 text-sm text-[rgb(var(--color-text-tertiary))]">Points</th>
                <th className="text-left py-3 px-4 text-sm text-[rgb(var(--color-text-tertiary))]">Streak</th>
                <th className="text-left py-3 px-4 text-sm text-[rgb(var(--color-text-tertiary))]">Tasks</th>
                <th className="text-right py-3 px-4 text-sm text-[rgb(var(--color-text-tertiary))]">Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr
                  key={u.id}
                  className="border-b border-[rgb(var(--color-border))] hover:bg-[rgb(var(--color-surface-elevated))]"
                >
                  <td className="py-3 px-4">
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 gradient-primary rounded-full flex items-center justify-center text-white text-sm">
                        {u.username.charAt(0).toUpperCase()}
                      </div>
                      <span>{u.username}</span>
                    </div>
                  </td>

                  <td className="py-3 px-4">
                    <select
                      value={u.role}
                      disabled={actionLoading}
                      onChange={async (e) => {
                        const newRole = e.target.value as "admin" | "player";

                        setActionError(null);
                        setActionLoading(true);
                        try {
                          await onSetRole(u.id, newRole);
                        } catch (err: any) {
                          setActionError(err?.message || "Failed to change role");
                        } finally {
                          setActionLoading(false);
                        }
                      }}
                      className="px-3 py-1 bg-[rgb(var(--color-surface-elevated))] border border-[rgb(var(--color-border))] rounded text-sm focus:outline-none focus:ring-2 focus:ring-violet-500"
                    >
                      <option value="player">Player</option>
                      <option value="admin">Admin</option>
                    </select>

                  </td>

                  <td className="py-3 px-4">{u.level}</td>

                  <td className="py-3 px-4">
                    <RankBadge rank={u.rank} size="sm" showLabel={false} />
                  </td>

                  <td className="py-3 px-4">{(u.totalPoints || 0).toLocaleString()}</td>
                  <td className="py-3 px-4">{u.currentStreak}</td>
                  <td className="py-3 px-4">{u.tasksCompleted}</td>

                  <td className="py-3 px-4">
                    <div className="flex items-center justify-end gap-2">
                      <Button variant="ghost" size="sm" disabled={actionLoading} onClick={() => handleResetStats(u.id)}>
                        Reset Stats
                      </Button>

                      <Button variant="danger" size="sm" disabled={actionLoading} onClick={() => setShowDeleteConfirm(u.id)}>
                        <Trash2 size={16} />
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>

      {/* Delete Confirmation Modal */}
      {showDeleteConfirm && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <Card className="max-w-md w-full p-6">
            <div className="flex items-center gap-3 mb-4">
              <div className="w-12 h-12 bg-red-500/20 rounded-xl flex items-center justify-center">
                <AlertTriangle size={24} className="text-red-400" />
              </div>
              <h3 className="text-red-400">Delete User</h3>
            </div>

            <p className="text-[rgb(var(--color-text-secondary))] mb-6">
              Are you sure you want to delete this user? This action cannot be undone and will permanently delete all user data.
            </p>

            <div className="flex items-center gap-3">
              <Button variant="secondary" className="flex-1" disabled={actionLoading} onClick={() => setShowDeleteConfirm(null)}>
                Cancel
              </Button>
              <Button
                variant="danger"
                className="flex-1"
                disabled={actionLoading}
                onClick={() => handleDeleteUser(showDeleteConfirm)}
              >
                <Trash2 size={16} />
                Delete User
              </Button>
            </div>
          </Card>
        </div>
      )}

      {/* Create User Modal */}
      {showCreateUser && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <Card className="max-w-md w-full p-6">
            <div className="flex items-center justify-between mb-4">
              <h3>Create New User</h3>
              <button
                onClick={() => setShowCreateUser(false)}
                className="text-[rgb(var(--color-text-tertiary))] hover:text-[rgb(var(--color-text-primary))]"
              >
                <X size={20} />
              </button>
            </div>


            <div className="space-y-4 mb-6">
              <div>
                <label className="block text-sm mb-2 text-[rgb(var(--color-text-secondary))]">Username</label>
                <input
                  type="text"
                  value={createUsername}
                  onChange={(e) => setCreateUsername(e.target.value)}
                  placeholder="Enter username"
                  className="w-full px-4 py-2 bg-[rgb(var(--color-surface-elevated))] border border-[rgb(var(--color-border))] rounded-lg text-[rgb(var(--color-text-primary))] focus:outline-none focus:ring-2 focus:ring-violet-500"
                  autoFocus
                />
              </div>

              <div>
              <label className="block text-sm mb-2 text-[rgb(var(--color-text-secondary))]">Password</label>
              <input
                type="password"
                value={createPassword}
                onChange={(e) => setCreatePassword(e.target.value)}
                placeholder="Set a password"
                className="w-full px-4 py-2 bg-[rgb(var(--color-surface-elevated))] border border-[rgb(var(--color-border))] rounded-lg text-[rgb(var(--color-text-primary))] focus:outline-none focus:ring-2 focus:ring-violet-500"
              />
            </div>

              <div>
                <label className="block text-sm mb-2 text-[rgb(var(--color-text-secondary))]">Role</label>
                <select
                  value={createRole}
                  onChange={(e) => setCreateRole(e.target.value as "admin" | "player")}

                  className="w-full px-4 py-2 bg-[rgb(var(--color-surface-elevated))] border border-[rgb(var(--color-border))] rounded-lg text-[rgb(var(--color-text-primary))] focus:outline-none focus:ring-2 focus:ring-violet-500"
                >
                  <option value="player">Player</option>
                  <option value="admin">Admin</option>
                </select>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <Button variant="secondary" className="flex-1" disabled={actionLoading} onClick={() => setShowCreateUser(false)}>
                Cancel
              </Button>
              <Button className="flex-1" disabled={actionLoading} onClick={handleCreateUser}>
                <UserPlus size={16} />
                Create User
              </Button>
            </div>
          </Card>
        </div>
      )}
    </div>
  );
};
