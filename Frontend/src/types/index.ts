export type TaskStatus = 'pending' | 'in-progress' | 'completed' | 'overdue';
export type TaskType = 'daily' | 'weekly' | 'monthly' | 'one-time';
export type TaskDifficulty = 'easy' | 'medium' | 'hard';
export type TaskCategory = 'work' | 'study' | 'health' | 'personal' | 'other';
export type UserRole = 'player' | 'admin';
export type Rank = 'bronze' | 'silver' | 'gold' | 'platinum' | 'diamond';
export type AchievementRarity = 'common' | 'rare' | 'epic' | 'legendary';

export interface Task {
  id: string;
  title: string;
  description: string;
  status: TaskStatus;
  dueDate: string;
  type: TaskType;
  difficulty: TaskDifficulty;
  category: TaskCategory;
  points: number;
  createdAt: string;
  completedAt?: string;
  recurring: boolean;
}

export interface User {
  id: string;
  username: string;
  role: UserRole;
  level: number;
  xp: number;
  rank: Rank;
  currentStreak: number;
  longestStreak: number;
  totalPoints: number;
  tasksCompleted: number;
  avatar?: string;
}

export interface Achievement {
  id: string;
  name: string;
  description: string;
  icon: string;
  rarity: AchievementRarity;
  unlocked: boolean;
  unlockedAt?: string;
  category: string;
}

export interface Quest {
  id: string;
  title: string;
  description: string;
  type: 'daily' | 'weekly';
  progress: number;
  target: number;
  reward: number;
  expiresAt: string;
  completed: boolean;
}

export interface LeaderboardEntry {
  user: User;
  position: number;
}
