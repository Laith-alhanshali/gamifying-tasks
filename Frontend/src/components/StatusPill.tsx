import React from 'react';
import { TaskStatus } from '../types';

interface StatusPillProps {
  status: TaskStatus;
}

export const StatusPill: React.FC<StatusPillProps> = ({ status }) => {
  const styles = {
    pending: 'bg-slate-500/20 text-slate-300 border-slate-500/30',
    'in-progress': 'bg-blue-500/20 text-blue-300 border-blue-500/30',
    completed: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
    overdue: 'bg-orange-500/20 text-orange-400 border-orange-500/30',
  };

  const labels = {
    pending: 'Pending',
    'in-progress': 'In Progress',
    completed: 'Completed',
    overdue: 'Overdue',
  };

  return (
    <span className={`inline-flex items-center px-3 py-1.5 rounded-full text-xs font-semibold border ${styles[status]}`}>
      {labels[status]}
    </span>
  );
};