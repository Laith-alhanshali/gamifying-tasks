import React, { useState } from 'react';
import { Task } from '../types';
import { Card } from '../components/Card';
import { ChevronLeft, ChevronRight, Circle } from 'lucide-react';
import { Button } from '../components/Button';
import { TaskCard } from '../components/TaskCard';

interface CalendarScreenProps {
  tasks: Task[];
  onCompleteTask: (taskId: string) => void;
  onViewTask: (taskId: string) => void;
}

export const CalendarScreen: React.FC<CalendarScreenProps> = ({ tasks, onCompleteTask, onViewTask }) => {
  const [currentDate, setCurrentDate] = useState(new Date());
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);

  const getDaysInMonth = (date: Date) => {
    const year = date.getFullYear();
    const month = date.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const daysInMonth = lastDay.getDate();
    const startingDayOfWeek = firstDay.getDay();

    return { daysInMonth, startingDayOfWeek };
  };

  const getTasksForDate = (date: Date) => {
    const dateStr = date.toDateString();
    return tasks.filter((task) => new Date(task.dueDate).toDateString() === dateStr);
  };

  const { daysInMonth, startingDayOfWeek } = getDaysInMonth(currentDate);
  const monthName = currentDate.toLocaleString('default', { month: 'long', year: 'numeric' });

  const previousMonth = () => {
    setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1));
  };

  const nextMonth = () => {
    setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1));
  };

  const overdueTasks = tasks.filter((t) => t.status === 'overdue');
  const selectedDateTasks = selectedDate ? getTasksForDate(selectedDate) : [];

  return (
    <div className="space-y-6">
      <div>
        <h1>Calendar</h1>
        <p className="text-[rgb(var(--color-text-secondary))] mt-1">View and manage tasks by date</p>
      </div>

      {/* Overdue Tasks Alert */}
      {overdueTasks.length > 0 && (
        <Card className="p-4 border-red-500/50 bg-red-500/10">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-red-500/20 rounded-lg flex items-center justify-center">
              <Circle size={20} className="text-red-400 fill-red-400" />
            </div>
            <div>
              <h4 className="text-red-400">You have {overdueTasks.length} overdue task{overdueTasks.length !== 1 ? 's' : ''}</h4>
              <p className="text-sm text-[rgb(var(--color-text-tertiary))]">Complete them to maintain your streak</p>
            </div>
          </div>
        </Card>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Calendar */}
        <div className="lg:col-span-2">
          <Card className="p-6">
            {/* Calendar Header */}
            <div className="flex items-center justify-between mb-6">
              <h3>{monthName}</h3>
              <div className="flex items-center gap-2">
                <Button variant="ghost" size="sm" onClick={previousMonth}>
                  <ChevronLeft size={20} />
                </Button>
                <Button variant="secondary" size="sm" onClick={() => setCurrentDate(new Date())}>
                  Today
                </Button>
                <Button variant="ghost" size="sm" onClick={nextMonth}>
                  <ChevronRight size={20} />
                </Button>
              </div>
            </div>

            {/* Calendar Grid */}
            <div className="grid grid-cols-7 gap-2">
              {/* Day Headers */}
              {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map((day) => (
                <div key={day} className="text-center text-sm text-[rgb(var(--color-text-tertiary))] py-2">
                  {day}
                </div>
              ))}

              {/* Empty cells for days before month starts */}
              {Array.from({ length: startingDayOfWeek }).map((_, i) => (
                <div key={`empty-${i}`} className="aspect-square" />
              ))}

              {/* Calendar Days */}
              {Array.from({ length: daysInMonth }).map((_, i) => {
                const day = i + 1;
                const date = new Date(currentDate.getFullYear(), currentDate.getMonth(), day);
                const tasksForDay = getTasksForDate(date);
                const isToday = date.toDateString() === new Date().toDateString();
                const isSelected = selectedDate?.toDateString() === date.toDateString();
                const hasOverdue = tasksForDay.some((t) => t.status === 'overdue');
                const hasCompleted = tasksForDay.some((t) => t.status === 'completed');

                return (
                  <button
                    key={day}
                    onClick={() => setSelectedDate(date)}
                    className={`aspect-square p-2 rounded-lg border transition-all ${
                      isSelected
                        ? 'bg-violet-500 text-white border-violet-500'
                        : isToday
                        ? 'border-violet-500 bg-violet-500/10'
                        : 'border-[rgb(var(--color-border))] hover:bg-[rgb(var(--color-surface-elevated))]'
                    }`}
                  >
                    <div className="flex flex-col items-center justify-center h-full">
                      <span className="text-sm">{day}</span>
                      {tasksForDay.length > 0 && (
                        <div className="flex gap-1 mt-1">
                          {hasOverdue && <Circle size={6} className="text-red-400 fill-red-400" />}
                          {hasCompleted && <Circle size={6} className="text-green-400 fill-green-400" />}
                          {!hasOverdue && !hasCompleted && <Circle size={6} className="text-blue-400 fill-blue-400" />}
                        </div>
                      )}
                    </div>
                  </button>
                );
              })}
            </div>

            {/* Legend */}
            <div className="flex items-center gap-4 mt-6 pt-6 border-t border-[rgb(var(--color-border))]">
              <div className="flex items-center gap-2 text-sm">
                <Circle size={12} className="text-red-400 fill-red-400" />
                <span className="text-[rgb(var(--color-text-secondary))]">Overdue</span>
              </div>
              <div className="flex items-center gap-2 text-sm">
                <Circle size={12} className="text-green-400 fill-green-400" />
                <span className="text-[rgb(var(--color-text-secondary))]">Completed</span>
              </div>
              <div className="flex items-center gap-2 text-sm">
                <Circle size={12} className="text-blue-400 fill-blue-400" />
                <span className="text-[rgb(var(--color-text-secondary))]">Pending</span>
              </div>
            </div>
          </Card>
        </div>

        {/* Selected Date Tasks */}
        <div>
          <Card className="p-6">
            <h3 className="mb-4">
              {selectedDate ? selectedDate.toLocaleDateString('en-US', { month: 'long', day: 'numeric' }) : 'Select a date'}
            </h3>
            <div className="space-y-3">
              {selectedDateTasks.length > 0 ? (
                selectedDateTasks.map((task) => (
                  <TaskCard key={task.id} task={task} onComplete={onCompleteTask} onView={onViewTask} />
                ))
              ) : (
                <div className="text-center py-8 text-[rgb(var(--color-text-tertiary))]">
                  <p className="text-sm">
                    {selectedDate ? 'No tasks for this date' : 'Select a date to view tasks'}
                  </p>
                </div>
              )}
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
};
