import React, { useState } from 'react';
import { Task, TaskStatus, TaskCategory, TaskDifficulty } from '../types';
import { Card } from '../components/Card';
import { Button } from '../components/Button';
import { TaskCard } from '../components/TaskCard';
import { Plus, Grid3x3, List, Search, Filter } from 'lucide-react';

interface TasksScreenProps {
  tasks: Task[];
  onCompleteTask: (taskId: string) => void;
  onEditTask: (taskId: string) => void;
  onViewTask: (taskId: string) => void;
  onCreateTask: () => void;
}

export const TasksScreen: React.FC<TasksScreenProps> = ({
  tasks,
  onCompleteTask,
  onEditTask,
  onViewTask,
  onCreateTask,
}) => {
  const [viewMode, setViewMode] = useState<'card' | 'list'>('card');
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<TaskStatus | 'all'>('all');
  const [categoryFilter, setCategoryFilter] = useState<TaskCategory | 'all'>('all');
  const [difficultyFilter, setDifficultyFilter] = useState<TaskDifficulty | 'all'>('all');

  const filteredTasks = tasks.filter((task) => {
    const matchesSearch = task.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      task.description.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesStatus = statusFilter === 'all' || task.status === statusFilter;
    const matchesCategory = categoryFilter === 'all' || task.category === categoryFilter;
    const matchesDifficulty = difficultyFilter === 'all' || task.difficulty === difficultyFilter;

    return matchesSearch && matchesStatus && matchesCategory && matchesDifficulty;
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="bg-gradient-to-r from-violet-400 via-purple-400 to-blue-400 bg-clip-text text-transparent">
            Task Board
          </h1>
          <p className="text-[rgb(var(--color-text-secondary))] mt-1">
            Complete quests to earn XP and level up! ⚔️
          </p>
        </div>
        <Button onClick={onCreateTask} className="shadow-lg shadow-violet-500/30">
          <Plus size={20} />
          New Task
        </Button>
      </div>

      {/* Filters */}
      <Card className="p-6">
        <div className="flex flex-col lg:flex-row gap-4">
          {/* Search */}
          <div className="flex-1">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-[rgb(var(--color-text-tertiary))]" size={20} />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search tasks..."
                className="w-full pl-10 pr-4 py-2 bg-[rgb(var(--color-surface-elevated))] border border-[rgb(var(--color-border))] rounded-lg text-[rgb(var(--color-text-primary))] placeholder:text-[rgb(var(--color-text-tertiary))] focus:outline-none focus:ring-2 focus:ring-violet-500"
              />
            </div>
          </div>

          {/* Status Filter */}
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as TaskStatus | 'all')}
            className="px-4 py-2 bg-[rgb(var(--color-surface-elevated))] border border-[rgb(var(--color-border))] rounded-lg text-[rgb(var(--color-text-primary))] focus:outline-none focus:ring-2 focus:ring-violet-500"
          >
            <option value="all">All Status</option>
            <option value="pending">Pending</option>
            <option value="in-progress">In Progress</option>
            <option value="completed">Completed</option>
            <option value="overdue">Overdue</option>
          </select>

          {/* Category Filter */}
          <select
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value as TaskCategory | 'all')}
            className="px-4 py-2 bg-[rgb(var(--color-surface-elevated))] border border-[rgb(var(--color-border))] rounded-lg text-[rgb(var(--color-text-primary))] focus:outline-none focus:ring-2 focus:ring-violet-500"
          >
            <option value="all">All Categories</option>
            <option value="work">Work</option>
            <option value="study">Study</option>
            <option value="health">Health</option>
            <option value="personal">Personal</option>
            <option value="other">Other</option>
          </select>

          {/* Difficulty Filter */}
          <select
            value={difficultyFilter}
            onChange={(e) => setDifficultyFilter(e.target.value as TaskDifficulty | 'all')}
            className="px-4 py-2 bg-[rgb(var(--color-surface-elevated))] border border-[rgb(var(--color-border))] rounded-lg text-[rgb(var(--color-text-primary))] focus:outline-none focus:ring-2 focus:ring-violet-500"
          >
            <option value="all">All Difficulty</option>
            <option value="easy">Easy</option>
            <option value="medium">Medium</option>
            <option value="hard">Hard</option>
          </select>

          {/* View Toggle */}
          <div className="flex items-center gap-2 bg-[rgb(var(--color-surface-elevated))] rounded-lg p-1">
            <button
              onClick={() => setViewMode('card')}
              className={`p-2 rounded ${viewMode === 'card' ? 'bg-violet-500 text-white' : 'text-[rgb(var(--color-text-secondary))]'}`}
            >
              <Grid3x3 size={20} />
            </button>
            <button
              onClick={() => setViewMode('list')}
              className={`p-2 rounded ${viewMode === 'list' ? 'bg-violet-500 text-white' : 'text-[rgb(var(--color-text-secondary))]'}`}
            >
              <List size={20} />
            </button>
          </div>
        </div>
      </Card>

      {/* Results Count */}
      <div className="flex items-center justify-between">
        <p className="text-sm text-[rgb(var(--color-text-secondary))]">
          {filteredTasks.length} task{filteredTasks.length !== 1 ? 's' : ''} found
        </p>
      </div>

      {/* Tasks Grid/List */}
      {filteredTasks.length > 0 ? (
        <div className={viewMode === 'card' ? 'grid grid-cols-1 lg:grid-cols-2 gap-4' : 'space-y-3'}>
          {filteredTasks.map((task) => (
            <TaskCard
              key={task.id}
              task={task}
              onComplete={onCompleteTask}
              onEdit={onEditTask}
              onView={onViewTask}
            />
          ))}
        </div>
      ) : (
        <Card className="p-12">
          <div className="text-center text-[rgb(var(--color-text-tertiary))]">
            <Filter size={48} className="mx-auto mb-4 opacity-50" />
            <h3 className="mb-2">No tasks found</h3>
            <p className="text-sm">Try adjusting your filters or create a new task</p>
          </div>
        </Card>
      )}
    </div>
  );
};