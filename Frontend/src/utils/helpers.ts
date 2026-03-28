import { Task, Rank } from '../types';
import { rankThresholds, xpPerLevel } from '../data/mockData';

export const getDueDateLabel = (dueDate: string): { label: string; color: string } => {
  const now = new Date();
  const due = new Date(dueDate);
  const diffMs = due.getTime() - now.getTime();
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60));

  if (diffMs < 0) {
    const overdueDays = Math.abs(diffDays);
    return {
      label: overdueDays === 0 ? 'Overdue today' : `Overdue by ${overdueDays} day${overdueDays > 1 ? 's' : ''}`,
      color: 'danger',
    };
  }

  if (diffDays === 0) {
    if (diffHours < 1) {
      return { label: 'Due in < 1 hour', color: 'warning' };
    }
    return { label: 'Due today', color: 'warning' };
  }

  if (diffDays === 1) {
    return { label: 'Due tomorrow', color: 'warning' };
  }

  if (diffDays <= 3) {
    return { label: `Due in ${diffDays} days`, color: 'primary' };
  }

  return { label: `Due in ${diffDays} days`, color: 'neutral' };
};

export const calculateNextRank = (totalPoints: number): { currentRank: Rank; nextRank: Rank | null; progress: number } => {
  const ranks: Rank[] = ['bronze', 'silver', 'gold', 'platinum', 'diamond'];
  
  let currentRank: Rank = 'bronze';
  for (const rank of ranks) {
    if (totalPoints >= rankThresholds[rank]) {
      currentRank = rank;
    }
  }

  const currentIndex = ranks.indexOf(currentRank);
  const nextRank = currentIndex < ranks.length - 1 ? ranks[currentIndex + 1] : null;

  if (!nextRank) {
    return { currentRank, nextRank: null, progress: 100 };
  }

  const currentThreshold = rankThresholds[currentRank];
  const nextThreshold = rankThresholds[nextRank];
  const progress = ((totalPoints - currentThreshold) / (nextThreshold - currentThreshold)) * 100;

  return { currentRank, nextRank, progress: Math.min(progress, 100) };
};

export const calculateLevel = (xp: number): { level: number; progress: number } => {
  const level = Math.floor(xp / xpPerLevel) + 1;
  const progress = (xp % xpPerLevel / xpPerLevel) * 100;
  return { level, progress };
};

export const getRankColor = (rank: Rank): string => {
  const colors = {
    bronze: 'rgb(205, 127, 50)',
    silver: 'rgb(192, 192, 192)',
    gold: 'rgb(255, 215, 0)',
    platinum: 'rgb(229, 228, 226)',
    diamond: 'rgb(185, 242, 255)',
  };
  return colors[rank];
};

export const getRarityColor = (rarity: string): string => {
  const colors = {
    common: 'rgb(148, 163, 184)',
    rare: 'rgb(59, 130, 246)',
    epic: 'rgb(168, 85, 247)',
    legendary: 'rgb(251, 191, 36)',
  };
  return colors[rarity as keyof typeof colors] || colors.common;
};

export const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMs / 3600000);
  const diffDays = Math.floor(diffMs / 86400000);

  if (diffMins < 1) return 'Just now';
  if (diffMins < 60) return `${diffMins}m ago`;
  if (diffHours < 24) return `${diffHours}h ago`;
  if (diffDays < 7) return `${diffDays}d ago`;

  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
};

export const getStatusColor = (status: Task['status']): string => {
  const colors = {
    pending: 'rgb(148, 163, 184)',
    'in-progress': 'rgb(59, 130, 246)',
    completed: 'rgb(34, 197, 94)',
    overdue: 'rgb(239, 68, 68)',
  };
  return colors[status];
};

export const getCategoryIcon = (category: Task['category']): string => {
  const icons = {
    work: 'briefcase',
    study: 'book-open',
    health: 'heart',
    personal: 'user',
    other: 'circle',
  };
  return icons[category];
};
