import React from 'react';
import { Task } from '../types';
import { Card } from './Card';
import { StatusPill } from './StatusPill';
import { getDueDateLabel, getCategoryIcon } from '../utils/helpers';
import { Clock, Zap, CheckCircle2, Edit2, AlertCircle, Sparkles } from 'lucide-react';
import * as LucideIcons from 'lucide-react';
import { Button } from './Button';

interface TaskCardProps {
  task: Task;
  onComplete?: (taskId: string) => void;
  onEdit?: (taskId: string) => void;
  onView?: (taskId: string) => void;
}

export const TaskCard: React.FC<TaskCardProps> = ({ task, onComplete, onEdit, onView }) => {
  const dueDateInfo = getDueDateLabel(task.dueDate);
  const categoryIconName = getCategoryIcon(task.category);
  const CategoryIcon = (LucideIcons as any)[
    categoryIconName.split('-').map((word: string) => word.charAt(0).toUpperCase() + word.slice(1)).join('')
  ] || LucideIcons.Circle;

  const difficultyColors = {
    easy: 'text-emerald-400',
    medium: 'text-amber-400',
    hard: 'text-pink-400',
  };

  const difficultyBg = {
    easy: 'bg-emerald-500/10',
    medium: 'bg-amber-500/10',
    hard: 'bg-pink-500/10',
  };

  const isOverdue = task.status === 'overdue';
  const isCompleted = task.status === 'completed';

  return (
    <Card
      className={`p-5 quest-card corner-decoration transition-all duration-300 hover:scale-[1.02] ${
        isOverdue ? 'overdue-glow' : ''
      } ${isCompleted ? 'opacity-75' : ''}`}
      onClick={onView ? () => onView(task.id) : undefined}
    >
      <div className="flex items-start gap-4 relative z-10">
        {/* Category Icon - Gaming Style */}
        <div className="relative">
          <div className="w-12 h-12 bg-gradient-to-br from-violet-500/20 to-blue-500/20 rounded-xl flex items-center justify-center border-2 border-violet-500/30 shadow-lg">
            <CategoryIcon size={22} className="text-violet-400" />
          </div>
          {/* Corner sparkles for high value tasks */}
          {task.points >= 100 && (
            <Sparkles size={14} className="absolute -top-1 -right-1 text-amber-400 animate-pulse" />
          )}
        </div>

        {/* Main Content */}
        <div className="flex-1 min-w-0">
          <div className="flex items-start justify-between gap-4 mb-3">
            <div className="flex-1">
              <h4 className="mb-1.5 font-bold">{task.title}</h4>
              <p className="text-sm text-[rgb(var(--color-text-tertiary))] line-clamp-2">{task.description}</p>
            </div>
            <StatusPill status={task.status} />
          </div>

          {/* Meta Info - Gaming Style */}
          <div className="flex flex-wrap items-center gap-3 mt-4">
            {/* XP Orb */}
            <div className="xp-orb">
              <Zap size={14} className="text-white" fill="white" />
              <span className="text-sm font-bold text-white">{task.points} XP</span>
            </div>

            {/* Due Date with glow for urgent */}
            <div className={`flex items-center gap-1.5 text-sm px-3 py-1.5 rounded-full ${
              isOverdue 
                ? 'bg-orange-500/20 border border-orange-400/40' 
                : 'bg-[rgb(var(--color-surface-elevated))]'
            }`}>
              <Clock size={14} className={isOverdue ? 'text-orange-400' : 'text-[rgb(var(--color-text-tertiary))]'} />
              <span className={isOverdue ? 'text-orange-400 font-semibold' : 'text-[rgb(var(--color-text-tertiary))]'}>
                {dueDateInfo.label}
              </span>
            </div>

            {/* Difficulty Badge */}
            <div className={`flex items-center gap-1.5 text-sm px-3 py-1.5 rounded-full ${difficultyBg[task.difficulty]} border border-current/20`}>
              <AlertCircle size={14} className={difficultyColors[task.difficulty]} />
              <span className={`${difficultyColors[task.difficulty]} font-semibold capitalize`}>{task.difficulty}</span>
            </div>

            {/* Type Tag */}
            <span className="text-xs px-3 py-1.5 rounded-full bg-violet-500/10 text-violet-400 border border-violet-500/20 font-medium">
              {task.type}
            </span>
          </div>
        </div>

        {/* Actions */}
        <div className="flex items-center gap-2 flex-shrink-0">
          {task.status !== 'completed' && onComplete && (
            <Button
              size="sm"
              variant="primary"
              className="shadow-lg shadow-violet-500/30"
              onClick={(e) => {
                e.stopPropagation();
                onComplete(task.id);
              }}
            >
              <CheckCircle2 size={16} />
              Complete
            </Button>
          )}
          {onEdit && (
            <Button
              size="sm"
              variant="ghost"
              onClick={(e) => {
                e.stopPropagation();
                onEdit(task.id);
              }}
            >
              <Edit2 size={16} />
            </Button>
          )}
        </div>
      </div>
    </Card>
  );
};
