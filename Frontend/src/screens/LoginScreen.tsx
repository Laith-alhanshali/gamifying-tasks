import React, { useState } from 'react';
import { Trophy, User as UserIcon, Lock } from 'lucide-react';
import { Button } from '../components/Button';
import { Card } from '../components/Card';
import { User } from '../types';

interface LoginScreenProps {
  onLogin: (user: User) => void;
}

type MeProfile = {
  authenticated: boolean;
  userId: number;
  username: string | null;
  role: string | null; // backend returns "ADMIN" / "PLAYER" (but keep flexible)
  totalPoints: number;
  xp: number;
  level: number;
  rank: string | null; // backend returns "BRONZE" etc (but keep flexible)
  currentStreak: number;
  longestStreak: number;
  tasksCompleted: number;
  achievementsCount: number;
};

export const LoginScreen: React.FC<LoginScreenProps> = ({ onLogin }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const roleToFrontend = (role: string | null | undefined): User['role'] => {
    return (role ?? '').toUpperCase() === 'ADMIN' ? 'admin' : 'player';
  };

  const rankToFrontend = (rank: string | null | undefined): User['rank'] => {
    const r = (rank ?? '').toLowerCase();
    if (r === 'bronze' || r === 'silver' || r === 'gold' || r === 'platinum' || r === 'diamond') return r;
    return 'bronze';
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    const u = username.trim();
    const p = password; // backend decides if empty allowed

    // ✅ prevent backend @NotBlank error spam
    if (!u) {
      setError('Please enter a username.');
      return;
    }

    setLoading(true);

    try {
      // 1) login -> sets cookie
      const loginRes = await fetch('/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ username: u, password: p }),
      });

      if (!loginRes.ok) {
        const msg = await loginRes.text().catch(() => 'Invalid credentials');
        throw new Error(msg || 'Invalid credentials');
      }

      // 2) fetch full profile (real stats)
      const profileRes = await fetch('/me/profile', {
        method: 'GET',
        credentials: 'include',
      });

      if (!profileRes.ok) {
        const msg = await profileRes.text().catch(() => 'Failed to fetch /me/profile');
        throw new Error(msg || 'Failed to fetch /me/profile');
      }

      const profile: MeProfile = await profileRes.json();

      if (!profile.authenticated) throw new Error('Not authenticated');

      // ✅ build frontend User using real backend stats
      const loggedInUser: User = {
        id: String(profile.userId),
        username: profile.username ?? u, // fallback
        role: roleToFrontend(profile.role),
        level: profile.level ?? 1,
        xp: profile.totalPoints ?? 0, // ✅ XP = totalPoints (your rule)
        rank: rankToFrontend(profile.rank),
        currentStreak: profile.currentStreak ?? 0,
        longestStreak: profile.longestStreak ?? 0,
        totalPoints: profile.totalPoints ?? 0,
        tasksCompleted: profile.tasksCompleted ?? 0,
      };

      onLogin(loggedInUser);
    } catch (err: any) {
      setError(err?.message || 'Something went wrong');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-gradient-to-br from-violet-900/20 via-[rgb(var(--color-background))] to-blue-900/20">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center gap-3 mb-4">
            <div className="w-16 h-16 gradient-primary rounded-2xl flex items-center justify-center shadow-2xl shadow-violet-500/50">
              <Trophy className="text-white" size={32} />
            </div>
            <h1 className="text-4xl gradient-primary text-gradient">TaskUp</h1>
          </div>
          <p className="text-[rgb(var(--color-text-secondary))] text-lg">
            Gamify your productivity and level up your life
          </p>
        </div>

        {/* Login Card */}
        <Card className="p-8 mb-6">
          <h2 className="mb-6 text-center">Welcome Back</h2>

          {/* Enter submits */}
          <form onSubmit={handleSubmit} className="space-y-4 mb-6">
            <div>
              <label className="block text-sm mb-2 text-[rgb(var(--color-text-secondary))]">Username</label>
              <div className="relative">
                <UserIcon
                  className="absolute left-3 top-1/2 -translate-y-1/2 text-[rgb(var(--color-text-tertiary))]"
                  size={20}
                />
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="Enter your username"
                  className="w-full pl-10 pr-4 py-3 bg-[rgb(var(--color-surface-elevated))] border border-[rgb(var(--color-border))] rounded-lg text-[rgb(var(--color-text-primary))] placeholder:text-[rgb(var(--color-text-tertiary))] focus:outline-none focus:ring-2 focus:ring-violet-500"
                  autoComplete="username"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm mb-2 text-[rgb(var(--color-text-secondary))]">Password</label>
              <div className="relative">
                <Lock
                  className="absolute left-3 top-1/2 -translate-y-1/2 text-[rgb(var(--color-text-tertiary))]"
                  size={20}
                />
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter your password"
                  className="w-full pl-10 pr-4 py-3 bg-[rgb(var(--color-surface-elevated))] border border-[rgb(var(--color-border))] rounded-lg text-[rgb(var(--color-text-primary))] placeholder:text-[rgb(var(--color-text-tertiary))] focus:outline-none focus:ring-2 focus:ring-violet-500"
                  autoComplete="current-password"
                />
              </div>
            </div>

            {error && <div className="text-sm text-red-400">{error}</div>}

            <Button className="w-full" size="lg" type="submit" disabled={loading}>
              {loading ? 'Logging in…' : 'Login'}
            </Button>
          </form>

          <p className="text-center text-sm text-[rgb(var(--color-text-tertiary))]">
            No account? Ask an admin to create one.
          </p>
        </Card>

        <p className="text-center text-sm text-[rgb(var(--color-text-tertiary))] mt-8">
          Where productivity feels rewarding
        </p>
      </div>
    </div>
  );
};
