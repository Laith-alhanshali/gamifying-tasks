import React from 'react';
import { Task } from '../types';
import { Card } from '../components/Card';
import { Button } from '../components/Button';
import { StatusPill } from '../components/StatusPill';
import { X, Calendar, Clock, Zap, Tag, AlertCircle, CheckCircle2 } from 'lucide-react';
import { getDueDateLabel, formatDate } from '../utils/helpers';

interface TaskDetailsModalProps {
  task: Task;
  onClose: () => void;
  onComplete?: (taskId: string) => void;
  onEdit?: (taskId: string) => void;
}

export const TaskDetailsModal: React.FC<TaskDetailsModalProps> = ({ task, onClose, onComplete, onEdit }) => {
  const dueDateInfo = getDueDateLabel(task.dueDate);

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4" onClick={onClose}>
      <div className="max-w-2xl w-full max-h-[90vh] overflow-y-auto" onClick={(e) => e.stopPropagation()}>
        <Card className="p-6">
          {/* Header */}
          <div className="flex items-start justify-between mb-6">
            <div className="flex-1">
              <div className="flex items-center gap-3 mb-2">
                <h2>{task.title}</h2>
                <StatusPill status={task.status} />
              </div>
              <p className="text-[rgb(var(--color-text-secondary))]">{task.description}</p>
            </div>
            <button
              onClick={onClose}
              className="p-2 rounded-lg text-[rgb(var(--color-text-tertiary))] hover:bg-[rgb(var(--color-surface-elevated))] transition-colors ml-4"
            >
              <X size={20} />
            </button>
          </div>

          {/* Meta Information Grid */}
          <div className="grid grid-cols-2 gap-4 mb-6">
            <div className="p-4 bg-[rgb(var(--color-surface-elevated))] rounded-lg">
              <div className="flex items-center gap-2 mb-2">
                <Calendar size={16} className="text-[rgb(var(--color-text-tertiary))]" />
                <span className="text-sm text-[rgb(var(--color-text-tertiary))]">Due Date</span>
              </div>
              <p className={dueDateInfo.color === 'danger' ? 'text-red-400' : ''}>
                {new Date(task.dueDate).toLocaleDateString('en-US', {
                  month: 'long',
                  day: 'numeric',
                  year: 'numeric',
                  hour: '2-digit',
                  minute: '2-digit',
                })}
              </p>
              <p className="text-xs text-[rgb(var(--color-text-tertiary))] mt-1">{dueDateInfo.label}</p>
            </div>

            <div className="p-4 bg-[rgb(var(--color-surface-elevated))] rounded-lg">
              <div className="flex items-center gap-2 mb-2">
                <Zap size={16} className="text-amber-400" />
                <span className="text-sm text-[rgb(var(--color-text-tertiary))]">Points Reward</span>
              </div>
              <p className="text-amber-400">{task.points} XP</p>
              <p className="text-xs text-[rgb(var(--color-text-tertiary))] mt-1">Upon completion</p>
            </div>

            <div className="p-4 bg-[rgb(var(--color-surface-elevated))] rounded-lg">
              <div className="flex items-center gap-2 mb-2">
                <Tag size={16} className="text-[rgb(var(--color-text-tertiary))]" />
                <span className="text-sm text-[rgb(var(--color-text-tertiary))]">Category</span>
              </div>
              <p className="capitalize">{task.category}</p>
              <p className="text-xs text-[rgb(var(--color-text-tertiary))] mt-1 capitalize">{task.type}</p>
            </div>

            <div className="p-4 bg-[rgb(var(--color-surface-elevated))] rounded-lg">
              <div className="flex items-center gap-2 mb-2">
                <AlertCircle size={16} className="text-[rgb(var(--color-text-tertiary))]" />
                <span className="text-sm text-[rgb(var(--color-text-tertiary))]">Difficulty</span>
              </div>
              <p className="capitalize">{task.difficulty}</p>
              {task.recurring && (
                <p className="text-xs text-violet-400 mt-1">♻ Recurring</p>
              )}
            </div>
          </div>

          {/* Timeline */}
          <div className="mb-6">
            <h4 className="mb-3">Timeline</h4>
            <div className="space-y-3">
              <div className="flex items-start gap-3">
                <div className="w-8 h-8 rounded-full bg-blue-500/20 flex items-center justify-center flex-shrink-0">
                  <Clock size={16} className="text-blue-400" />
                </div>
                <div>
                  <p className="text-sm">Created</p>
                  <p className="text-xs text-[rgb(var(--color-text-tertiary))]">
                    {new Date(task.createdAt).toLocaleDateString('en-US', {
                      month: 'long',
                      day: 'numeric',
                      year: 'numeric',
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </p>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <div className={`w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 ${
                  task.status === 'overdue' ? 'bg-red-500/20' : 'bg-amber-500/20'
                }`}>
                  <Calendar size={16} className={task.status === 'overdue' ? 'text-red-400' : 'text-amber-400'} />
                </div>
                <div>
                  <p className="text-sm">Due Date</p>
                  <p className="text-xs text-[rgb(var(--color-text-tertiary))]">
                    {new Date(task.dueDate).toLocaleDateString('en-US', {
                      month: 'long',
                      day: 'numeric',
                      year: 'numeric',
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </p>
                </div>
              </div>

              {task.completedAt && (
                <div className="flex items-start gap-3">
                  <div className="w-8 h-8 rounded-full bg-green-500/20 flex items-center justify-center flex-shrink-0">
                    <CheckCircle2 size={16} className="text-green-400" />
                  </div>
                  <div>
                    <p className="text-sm">Completed</p>
                    <p className="text-xs text-[rgb(var(--color-text-tertiary))]">
                      {new Date(task.completedAt).toLocaleDateString('en-US', {
                        month: 'long',
                        day: 'numeric',
                        year: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </p>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Notes Section */}
          <div className="mb-6">
            <h4 className="mb-3">Notes</h4>
            <textarea
              placeholder="Add notes about this task..."
              rows={4}
              className="w-full px-4 py-3 bg-[rgb(var(--color-surface-elevated))] border border-[rgb(var(--color-border))] rounded-lg text-[rgb(var(--color-text-primary))] placeholder:text-[rgb(var(--color-text-tertiary))] focus:outline-none focus:ring-2 focus:ring-violet-500 resize-none"
            />
          </div>

          {/* Actions */}
          <div className="flex items-center gap-3">
            {task.status !== 'completed' && onComplete && (
              <Button
                className="flex-1"
                onClick={() => {
                  onComplete(task.id);
                  onClose();
                }}
              >
                <CheckCircle2 size={20} />
                Mark as Complete
              </Button>
            )}
            {onEdit && (
              <Button
                variant="secondary"
                className="flex-1"
                onClick={() => {
                  onEdit(task.id);
                  onClose();
                }}
              >
                Edit Task
              </Button>
            )}
            <Button variant="ghost" onClick={onClose}>
              Close
            </Button>
          </div>
        </Card>
      </div>
    </div>
  );
};
