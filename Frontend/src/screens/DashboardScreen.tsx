import React from 'react';
import { User, Task, Achievement, Quest } from '../types';
import { StatCard } from '../components/StatCard';
import { Card } from '../components/Card';
import { RankBadge } from '../components/RankBadge';
import { ProgressBar } from '../components/ProgressBar';
import { TaskCard } from '../components/TaskCard';
import { Trophy, Zap, Flame, CheckCircle2, Target, Gift } from 'lucide-react';
import { calculateNextRank, getRarityColor } from '../utils/helpers';
import * as LucideIcons from 'lucide-react';

interface DashboardScreenProps {
  user: User;
  tasks: Task[];
  achievements: Achievement[];
  quests: Quest[];
  onCompleteTask: (taskId: string) => void;
  onViewTask: (taskId: string) => void;
}

export const DashboardScreen: React.FC<DashboardScreenProps> = ({
  user,
  tasks,
  achievements,
  quests,
  onCompleteTask,
  onViewTask,
}) => {
  const rankInfo = calculateNextRank(user.totalPoints);
  const todayTasks = tasks.filter((t) => {
    const today = new Date().toDateString();
    return new Date(t.dueDate).toDateString() === today;
  });
  const overdueTasks = tasks.filter((t) => t.status === 'overdue');
  const completedThisWeek = tasks.filter((t) => {
    if (!t.completedAt) return false;
    const weekAgo = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);
    return new Date(t.completedAt) >= weekAgo;
  }).length;

  const recentAchievements = achievements
    .filter((a) => a.unlocked)
    .sort((a, b) => (b.unlockedAt && a.unlockedAt ? new Date(b.unlockedAt).getTime() - new Date(a.unlockedAt).getTime() : 0))
    .slice(0, 3);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="flex items-center gap-3">
            <span className="bg-gradient-to-r from-violet-400 via-purple-400 to-blue-400 bg-clip-text text-transparent">
              Dashboard
            </span>
          </h1>
          <p className="text-[rgb(var(--color-text-secondary))] mt-2 text-lg">
            Welcome back, <span className="font-bold text-violet-400">{user.username}</span>! Ready to level up? 🎮✨
          </p>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard title="Level" value={user.level} icon={Zap} color="primary" subtitle={`${user.xp} XP`} />
        <StatCard
          title="Current Streak"
          value={`${user.currentStreak} days`}
          icon={Flame}
          color="warning"
          subtitle={`Longest: ${user.longestStreak} days`}
        />
        <StatCard
          title="Completed This Week"
          value={completedThisWeek}
          icon={CheckCircle2}
          color="success"
          subtitle={`${user.tasksCompleted} total`}
        />
        <div>
          <Card className="p-6 h-full gaming-card hover:scale-105 transition-transform duration-300" hover>
            <div className="flex items-center justify-between mb-3">
              <p className="text-sm text-[rgb(var(--color-text-tertiary))] uppercase tracking-wide font-medium">Rank</p>
              <RankBadge rank={user.rank} size="sm" showLabel={false} />
            </div>
            <h3 className="capitalize mb-2 bg-gradient-to-r from-violet-400 to-blue-400 bg-clip-text text-transparent font-bold">{user.rank}</h3>
            {rankInfo.nextRank && (
              <p className="text-xs text-[rgb(var(--color-text-tertiary))] font-medium">
                {Math.round(rankInfo.progress)}% to {rankInfo.nextRank}
              </p>
            )}
          </Card>
        </div>
      </div>

      {/* Main Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column - Tasks */}
        <div className="lg:col-span-2 space-y-6">
          {/* Today's Focus */}
          <Card className="p-6">
            <div className="flex items-center justify-between mb-4">
              <h3>Today's Focus</h3>
              <span className="text-sm text-[rgb(var(--color-text-tertiary))]">{todayTasks.length} tasks</span>
            </div>
            <div className="space-y-3">
              {todayTasks.slice(0, 3).map((task) => (
                <TaskCard key={task.id} task={task} onComplete={onCompleteTask} onView={onViewTask} />
              ))}
              {todayTasks.length === 0 && (
                <div className="text-center py-8 text-[rgb(var(--color-text-tertiary))]">
                  <CheckCircle2 size={48} className="mx-auto mb-2 opacity-50" />
                  <p>No tasks due today. Great job! 🎉</p>
                </div>
              )}
            </div>
          </Card>

          {/* Overdue Tasks */}
          {overdueTasks.length > 0 && (
            <Card className="p-6 border-2 border-orange-500/30 bg-gradient-to-br from-orange-500/5 to-amber-500/5 shadow-lg shadow-orange-500/10">
              <div className="flex items-center justify-between mb-4">
                <div className="flex items-center gap-2">
                  <h3 className="text-orange-400">⚠️ Overdue Quests</h3>
                </div>
                <span className="text-sm font-bold text-orange-400 bg-orange-500/20 px-3 py-1 rounded-full border border-orange-400/30">
                  {overdueTasks.length} task{overdueTasks.length !== 1 ? 's' : ''}
                </span>
              </div>
              <div className="space-y-3">
                {overdueTasks.slice(0, 2).map((task) => (
                  <TaskCard key={task.id} task={task} onComplete={onCompleteTask} onView={onViewTask} />
                ))}
              </div>
            </Card>
          )}
        </div>

        {/* Right Column - Quests & Achievements */}
        <div className="space-y-6">
          {/* Quest Progress */}
          <Card className="p-6 gaming-card">
            <div className="flex items-center gap-2 mb-4">
              <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-violet-500 to-blue-500 flex items-center justify-center shadow-lg shadow-violet-500/50">
                <Target size={18} className="text-white" />
              </div>
              <h3>Daily Quests</h3>
            </div>
            <div className="space-y-4">
              {quests.map((quest) => (
                <div key={quest.id} className="p-3 rounded-lg bg-gradient-to-r from-violet-500/5 to-blue-500/5 border border-violet-500/20">
                  <div className="flex items-start justify-between mb-3">
                    <div className="flex-1">
                      <h4 className="text-sm font-bold">{quest.title}</h4>
                      <p className="text-xs text-[rgb(var(--color-text-tertiary))] mt-1 font-medium">
                        {quest.progress}/{quest.target} completed
                      </p>
                    </div>
                    <div className="xp-orb text-xs">
                      <Gift size={12} className="text-white" />
                      <span className="font-bold text-white">{quest.reward}</span>
                    </div>
                  </div>
                  <ProgressBar progress={(quest.progress / quest.target) * 100} color="primary" />
                </div>
              ))}
            </div>
          </Card>

          {/* Rank Progress */}
          <Card className="p-6">
            <div className="flex items-center gap-2 mb-4">
              <Trophy size={20} className="text-violet-400" />
              <h3>Rank Progress</h3>
            </div>
            <div className="mb-4">
              <RankBadge rank={user.rank} size="md" />
            </div>
            {rankInfo.nextRank && (
              <>
                <p className="text-sm text-[rgb(var(--color-text-tertiary))] mb-2">
                  Progress to {rankInfo.nextRank}
                </p>
                <ProgressBar progress={rankInfo.progress} color="warning" showLabel />
              </>
            )}
            {!rankInfo.nextRank && (
              <p className="text-sm text-[rgb(var(--color-text-tertiary))]">
                🏆 You've reached the highest rank!
              </p>
            )}
          </Card>

          {/* Recent Achievements */}
          <Card className="p-6 gaming-card">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <Trophy size={20} className="text-amber-400" />
                <h3>Recent Achievements</h3>
              </div>
            </div>
            <div className="space-y-3">
              {recentAchievements.map((achievement) => {
                const IconComponent = (LucideIcons as any)[
                  achievement.icon.split('-').map((word: string) => word.charAt(0).toUpperCase() + word.slice(1)).join('')
                ] || LucideIcons.Award;

                return (
                  <div key={achievement.id} className="flex items-center gap-3 p-3 rounded-lg bg-gradient-to-r from-violet-500/10 to-blue-500/10 border border-violet-500/20 hover:border-violet-400/40 transition-all duration-300">
                    <div
                      className="w-12 h-12 rounded-lg flex items-center justify-center shadow-lg relative overflow-hidden"
                      style={{ 
                        backgroundColor: `${getRarityColor(achievement.rarity)}20`,
                        boxShadow: `0 0 20px ${getRarityColor(achievement.rarity)}40`
                      }}
                    >
                      <IconComponent size={24} style={{ color: getRarityColor(achievement.rarity) }} />
                      <div className="absolute inset-0 bg-gradient-to-tr from-transparent via-white/20 to-transparent" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <h4 className="text-sm font-bold">{achievement.name}</h4>
                      <p className="text-xs font-medium uppercase tracking-wider" style={{ color: getRarityColor(achievement.rarity) }}>
                        {achievement.rarity}
                      </p>
                    </div>
                  </div>
                );
              })}
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
};