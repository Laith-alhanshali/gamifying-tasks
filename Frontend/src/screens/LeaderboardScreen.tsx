import React, { useState } from 'react';
import { User } from '../types';
import { Card } from '../components/Card';
import { RankBadge } from '../components/RankBadge';
import { Trophy, Flame, Zap, Medal, Crown } from 'lucide-react';

interface LeaderboardScreenProps {
  leaderboard: User[];
  currentUser: User;
}

export const LeaderboardScreen: React.FC<LeaderboardScreenProps> = ({ leaderboard, currentUser }) => {
  const [timeFilter, setTimeFilter] = useState<'all-time' | 'weekly' | 'monthly'>('all-time');

  const sortedLeaderboard = [...leaderboard].sort((a, b) => b.totalPoints - a.totalPoints);
  const currentUserPosition = sortedLeaderboard.findIndex((u) => u.id === currentUser.id) + 1;

  return (
    <div className="space-y-6">
      <div>
        <h1>Leaderboard</h1>
        <p className="text-[rgb(var(--color-text-secondary))] mt-1">
          Compete with others and climb the ranks
        </p>
      </div>

      {/* Time Filter */}
      <Card className="p-4">
        <div className="flex items-center justify-center gap-2">
          {(['all-time', 'weekly', 'monthly'] as const).map((filter) => (
            <button
              key={filter}
              onClick={() => setTimeFilter(filter)}
              className={`px-4 py-2 rounded-lg text-sm transition-all ${
                timeFilter === filter
                  ? 'gradient-primary text-white shadow-lg shadow-violet-500/30'
                  : 'bg-[rgb(var(--color-surface-elevated))] text-[rgb(var(--color-text-secondary))] hover:bg-[rgb(var(--color-border))]'
              }`}
            >
              {filter === 'all-time' ? 'All Time' : filter.charAt(0).toUpperCase() + filter.slice(1)}
            </button>
          ))}
        </div>
      </Card>

      {/* Your Rank */}
      <Card className="p-6 gradient-primary bg-opacity-10 border-violet-500/30">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="w-16 h-16 gradient-primary rounded-xl flex items-center justify-center shadow-lg shadow-violet-500/50">
              <Trophy size={32} className="text-white" />
            </div>
            <div>
              <h3 className="mb-1">Your Rank</h3>
              <p className="text-sm text-[rgb(var(--color-text-secondary))]">
                #{currentUserPosition} • {currentUser.totalPoints.toLocaleString()} points
              </p>
            </div>
          </div>
          <RankBadge rank={currentUser.rank} size="md" />
        </div>
      </Card>

      {/* Top 3 Podium */}
      {sortedLeaderboard.length >= 3 && (
        <div className="grid grid-cols-3 gap-4 items-end">
          {/* 2nd Place */}
          <Card className="p-6 bg-gradient-to-br from-gray-300/10 to-gray-500/10 border-gray-400/30">
            <div className="text-center">
              <div className="w-16 h-16 bg-gradient-to-br from-gray-300 to-gray-500 rounded-xl flex items-center justify-center mx-auto mb-3 shadow-lg">
                <Medal size={32} className="text-white" />
              </div>
              <div className="text-3xl mb-1">2nd</div>
              <h4 className="mb-2">{sortedLeaderboard[1].username}</h4>
              <div className="flex items-center justify-center gap-4 text-sm text-[rgb(var(--color-text-secondary))]">
                <div className="flex items-center gap-1">
                  <Zap size={14} className="text-amber-400" />
                  {sortedLeaderboard[1].totalPoints.toLocaleString()}
                </div>
                <div className="flex items-center gap-1">
                  <Flame size={14} className="text-orange-400" />
                  {sortedLeaderboard[1].currentStreak}
                </div>
              </div>
            </div>
          </Card>

          {/* 1st Place */}
          <Card className="p-6 bg-gradient-to-br from-yellow-400/10 to-yellow-600/10 border-yellow-500/50 transform scale-105">
            <div className="text-center">
              <div className="w-20 h-20 bg-gradient-to-br from-yellow-400 to-yellow-600 rounded-xl flex items-center justify-center mx-auto mb-3 shadow-2xl shadow-yellow-500/50">
                <Crown size={40} className="text-white" />
              </div>
              <div className="text-4xl mb-1 gradient-rank-gold text-gradient">1st</div>
              <h3 className="mb-2">{sortedLeaderboard[0].username}</h3>
              <div className="flex items-center justify-center gap-4 text-sm text-[rgb(var(--color-text-secondary))]">
                <div className="flex items-center gap-1">
                  <Zap size={14} className="text-amber-400" />
                  {sortedLeaderboard[0].totalPoints.toLocaleString()}
                </div>
                <div className="flex items-center gap-1">
                  <Flame size={14} className="text-orange-400" />
                  {sortedLeaderboard[0].currentStreak}
                </div>
              </div>
            </div>
          </Card>

          {/* 3rd Place */}
          <Card className="p-6 bg-gradient-to-br from-orange-600/10 to-orange-800/10 border-orange-500/30">
            <div className="text-center">
              <div className="w-16 h-16 bg-gradient-to-br from-orange-600 to-orange-800 rounded-xl flex items-center justify-center mx-auto mb-3 shadow-lg">
                <Medal size={32} className="text-white" />
              </div>
              <div className="text-3xl mb-1">3rd</div>
              <h4 className="mb-2">{sortedLeaderboard[2].username}</h4>
              <div className="flex items-center justify-center gap-4 text-sm text-[rgb(var(--color-text-secondary))]">
                <div className="flex items-center gap-1">
                  <Zap size={14} className="text-amber-400" />
                  {sortedLeaderboard[2].totalPoints.toLocaleString()}
                </div>
                <div className="flex items-center gap-1">
                  <Flame size={14} className="text-orange-400" />
                  {sortedLeaderboard[2].currentStreak}
                </div>
              </div>
            </div>
          </Card>
        </div>
      )}

      {/* Full Leaderboard */}
      <Card className="p-6">
        <h3 className="mb-4">Full Rankings</h3>
        <div className="space-y-2">
          {sortedLeaderboard.map((user, index) => {
            const isCurrentUser = user.id === currentUser.id;
            const position = index + 1;

            return (
              <div
                key={user.id}
                className={`flex items-center gap-4 p-4 rounded-lg transition-all ${
                  isCurrentUser
                    ? 'bg-violet-500/20 border border-violet-500/30'
                    : 'bg-[rgb(var(--color-surface-elevated))] hover:bg-[rgb(var(--color-border))]'
                }`}
              >
                {/* Position */}
                <div className="w-12 text-center">
                  <span className="text-xl">{position <= 3 ? '🏆' : `#${position}`}</span>
                </div>

                {/* User Info */}
                <div className="flex-1 flex items-center gap-3">
                  <div className="w-10 h-10 gradient-primary rounded-full flex items-center justify-center text-white">
                    {user.username.charAt(0).toUpperCase()}
                  </div>
                  <div>
                    <h4 className="text-sm">
                      {user.username}
                      {isCurrentUser && <span className="text-violet-400 ml-2">(You)</span>}
                    </h4>
                    <p className="text-xs text-[rgb(var(--color-text-tertiary))]">Level {user.level}</p>
                  </div>
                </div>

                {/* Rank Badge */}
                <RankBadge rank={user.rank} size="sm" showLabel={false} />

                {/* Stats */}
                <div className="flex items-center gap-4 text-sm">
                  <div className="flex items-center gap-1.5 text-amber-400">
                    <Zap size={16} />
                    <span>{user.totalPoints.toLocaleString()}</span>
                  </div>
                  <div className="flex items-center gap-1.5 text-orange-400">
                    <Flame size={16} />
                    <span>{user.currentStreak}</span>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </Card>
    </div>
  );
};
