import React from 'react';
import { Quest, Task } from '../types';
import { Card } from '../components/Card';
import { Button } from '../components/Button';
import { ProgressBar } from '../components/ProgressBar';
import { Target, Gift, Clock, Zap } from 'lucide-react';
import { TaskCard } from '../components/TaskCard';

interface QuestsScreenProps {
  quests: Quest[];
  tasks: Task[];
  onCompleteTask: (taskId: string) => void;
  onViewTask: (taskId: string) => void;
}

export const QuestsScreen: React.FC<QuestsScreenProps> = ({ quests, tasks, onCompleteTask, onViewTask }) => {
  const suggestedTasks = tasks.filter((t) => t.status !== 'completed').slice(0, 5);

  const formatTimeRemaining = (expiresAt: string) => {
    const now = new Date();
    const expires = new Date(expiresAt);
    const diffMs = expires.getTime() - now.getTime();
    const hours = Math.floor(diffMs / (1000 * 60 * 60));
    const days = Math.floor(hours / 24);

    if (days > 0) return `${days}d ${hours % 24}h`;
    return `${hours}h`;
  };

  return (
    <div className="space-y-6">
      <div>
        <h1>Quests</h1>
        <p className="text-[rgb(var(--color-text-secondary))] mt-1">
          Complete quests to earn bonus rewards and level up faster
        </p>
      </div>

      {/* Active Quests */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {quests.map((quest) => {
          const progress = (quest.progress / quest.target) * 100;
          const isComplete = quest.progress >= quest.target;

          return (
            <Card key={quest.id} className="p-6 relative overflow-hidden" hover>
              {/* Background Decoration */}
              <div className="absolute top-0 right-0 w-32 h-32 gradient-primary opacity-10 rounded-full blur-3xl" />

              {/* Quest Type Badge */}
              <div className="flex items-center justify-between mb-4">
                <span
                  className={`px-3 py-1 rounded-full text-xs ${
                    quest.type === 'daily'
                      ? 'bg-blue-500/20 text-blue-300 border border-blue-500/30'
                      : 'bg-purple-500/20 text-purple-300 border border-purple-500/30'
                  }`}
                >
                  {quest.type === 'daily' ? 'Daily Quest' : 'Weekly Challenge'}
                </span>
                {!isComplete && (
                  <div className="flex items-center gap-1.5 text-sm text-[rgb(var(--color-text-tertiary))]">
                    <Clock size={14} />
                    {formatTimeRemaining(quest.expiresAt)}
                  </div>
                )}
              </div>

              {/* Quest Details */}
              <div className="mb-4">
                <div className="flex items-start gap-3 mb-2">
                  <div className="w-12 h-12 gradient-primary rounded-xl flex items-center justify-center shadow-lg shadow-violet-500/30">
                    <Target size={24} className="text-white" />
                  </div>
                  <div className="flex-1">
                    <h3 className="mb-1">{quest.title}</h3>
                    <p className="text-sm text-[rgb(var(--color-text-secondary))]">{quest.description}</p>
                  </div>
                </div>
              </div>

              {/* Progress */}
              <div className="mb-4">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm text-[rgb(var(--color-text-secondary))]">
                    {quest.progress} / {quest.target} tasks
                  </span>
                  <span className="text-sm text-[rgb(var(--color-text-secondary))]">{Math.round(progress)}%</span>
                </div>
                <ProgressBar progress={progress} color={isComplete ? 'success' : 'primary'} />
              </div>

              {/* Reward */}
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Gift size={20} className="text-amber-400" />
                  <span className="text-amber-400">
                    {quest.reward} XP Reward
                  </span>
                </div>
                {isComplete && !quest.completed && (
                  <Button size="sm">
                    <Zap size={16} />
                    Claim Reward
                  </Button>
                )}
                {quest.completed && (
                  <span className="text-sm text-green-400 flex items-center gap-1.5">
                    ✓ Claimed
                  </span>
                )}
              </div>
            </Card>
          );
        })}
      </div>

      {/* Suggested Tasks */}
      <div>
        <Card className="p-6">
          <div className="flex items-center gap-2 mb-4">
            <Target size={20} className="text-violet-400" />
            <h3>Suggested Tasks to Complete</h3>
          </div>
          <p className="text-sm text-[rgb(var(--color-text-secondary))] mb-4">
            Complete these tasks to make progress on your quests
          </p>
          <div className="space-y-3">
            {suggestedTasks.map((task) => (
              <TaskCard key={task.id} task={task} onComplete={onCompleteTask} onView={onViewTask} />
            ))}
            {suggestedTasks.length === 0 && (
              <div className="text-center py-8 text-[rgb(var(--color-text-tertiary))]">
                <p className="text-sm">All caught up! Great work! 🎉</p>
              </div>
            )}
          </div>
        </Card>
      </div>

      {/* Quest Info */}
      <Card className="p-6 bg-gradient-to-br from-violet-500/10 to-blue-500/10 border-violet-500/20">
        <h3 className="mb-3">How Quests Work</h3>
        <div className="space-y-2 text-sm text-[rgb(var(--color-text-secondary))]">
          <p>• Daily Quests reset every 24 hours and offer quick rewards</p>
          <p>• Weekly Challenges span 7 days and provide bigger bonuses</p>
          <p>• Complete tasks to make progress on all active quests simultaneously</p>
          <p>• Claim rewards before quests expire to earn bonus XP</p>
        </div>
      </Card>
    </div>
  );
};
