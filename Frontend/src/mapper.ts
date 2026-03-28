// src/mappers.ts
import { Task } from './types';
import { BackendTask } from './api';

const mapType = (t: BackendTask['type']): Task['type'] => {
  switch (t) {
    case 'ONE_TIME': return 'one-time';
    case 'DAILY': return 'daily';
    case 'WEEKLY': return 'weekly';
    case 'MONTHLY': return 'monthly';
  }
};

const mapDifficulty = (d: BackendTask['difficulty']): Task['difficulty'] => d.toLowerCase() as Task['difficulty'];
const mapCategory = (c: BackendTask['category']): Task['category'] => c.toLowerCase() as Task['category'];

function calcPoints(difficulty: Task['difficulty'], type: Task['type']) {
  const base = difficulty === 'easy' ? 25 : difficulty === 'medium' ? 50 : 100;
  const mult =
    type === 'daily' ? 1.2 :
    type === 'weekly' ? 1.5 :
    type === 'monthly' ? 2 :
    1;
  return Math.round(base * mult);
}

function statusFromBackend(completed: boolean, dueDate: string | null): Task['status'] {
  if (completed) return 'completed';
  if (!dueDate) return 'pending';

  // dueDate from backend is YYYY-MM-DD; treat overdue if it's before today
  const due = new Date(dueDate + 'T23:59:59');
  const now = new Date();
  if (due.getTime() < now.getTime()) return 'overdue';
  return 'pending';
}

export function backendTaskToUi(t: BackendTask): Task {
  const type = mapType(t.type);
  const difficulty = mapDifficulty(t.difficulty);
  const category = mapCategory(t.category);

  const dueIso = t.dueDate ? new Date(t.dueDate + 'T12:00:00').toISOString() : new Date().toISOString();
  const createdIso = t.createdAt ? new Date(t.createdAt).toISOString() : new Date().toISOString();
  const completedIso = t.completedAt ? new Date(t.completedAt).toISOString() : undefined;

  return {
    id: String(t.taskId),
    title: t.title,
    description: t.description,
    status: statusFromBackend(t.completed, t.dueDate),
    dueDate: dueIso,
    type,
    difficulty,
    category,
    points: calcPoints(difficulty, type),
    createdAt: createdIso,
    completedAt: completedIso,
    recurring: type !== 'one-time',
  };
}
