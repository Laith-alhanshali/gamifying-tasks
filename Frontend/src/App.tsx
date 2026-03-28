import React, { useState, useEffect, useRef, useLayoutEffect } from "react";
import { User, Task, Achievement, Quest } from './types';
import {
  mockAchievements,
  mockQuests,
} from './data/mockData';
import { Sidebar } from './components/Sidebar';
import { Header } from './components/Header';
import { Confetti } from './components/Confetti';
import { TaskDetailsModal } from './components/TaskDetailsModal';
import { LoginScreen } from './screens/LoginScreen';
import { DashboardScreen } from './screens/DashboardScreen';
import { TasksScreen } from './screens/TasksScreen';
import { CalendarScreen } from './screens/CalendarScreen';
import { QuestsScreen } from './screens/QuestsScreen';
import { AchievementsScreen } from './screens/AchievementsScreen';
import { LeaderboardScreen } from './screens/LeaderboardScreen';
import { AdminScreen } from './screens/AdminScreen';
import { SettingsScreen } from './screens/SettingsScreen';
import { ShopScreen } from './screens/ShopScreen';
import { LayoutDashboard, CheckSquare, Calendar, Target, Settings } from 'lucide-react';
import {
  apiTasks,
  apiCompleteTask,
  apiLeaderboard,
  apiAdminUsers,
  apiAdminCreateUser,
  apiAdminResetUser,
  apiAdminDeleteUser,
  apiAdminSetRole,
  apiCreateTask,
  apiUpdateTask,
  apiAchievements
} from './api';
import { TaskFormModal } from "./components/CreateTaskModal";






type Screen =
  | 'dashboard'
  | 'tasks'
  | 'calendar'
  | 'quests'
  | 'achievements'
  | 'leaderboard'
  | 'shop'
  | 'admin'
  | 'settings';

type MeProfile = {
  authenticated: boolean;
  userId: number;
  username: string;
  role: 'ADMIN' | 'PLAYER';
  totalPoints: number;
  xp: number;
  level: number;
  rank: 'BRONZE' | 'SILVER' | 'GOLD' | 'PLATINUM' | 'DIAMOND';
  currentStreak: number;
  longestStreak: number;
  tasksCompleted: number;
  achievementsCount: number;
};

function roleToFrontend(role: string): User['role'] {
  return role?.toUpperCase() === 'ADMIN' ? 'admin' : 'player';
}

function rankToFrontend(rank: string): User['rank'] {
  const r = (rank || '').toLowerCase();
  if (r === 'bronze' || r === 'silver' || r === 'gold' || r === 'platinum' || r === 'diamond') return r;
  return 'bronze';
}

async function fetchMeProfile(): Promise<User> {
  const r = await fetch('/me/profile', { credentials: 'include' });
  if (!r.ok) throw new Error(await r.text());
  const prof: MeProfile = await r.json();

  if (!prof.authenticated) throw new Error('Not authenticated');

  return {
    id: String(prof.userId),
    username: prof.username,
    role: roleToFrontend(prof.role),
    level: prof.level,
    xp: prof.totalPoints, // ✅ xp == totalPoints (your rule)
    rank: rankToFrontend(prof.rank),
    currentStreak: prof.currentStreak,
    longestStreak: prof.longestStreak,
    totalPoints: prof.totalPoints,
    tasksCompleted: prof.tasksCompleted,
  };
}

export default function App() {
  const [user, setUser] = useState<User | null>(null);
  const [currentScreen, setCurrentScreen] = useState<Screen>('dashboard');
  const [tasks, setTasks] = useState<Task[]>([]);
  const [achievements, setAchievements] = useState<Achievement[]>(mockAchievements);
  const [quests, setQuests] = useState<Quest[]>(mockQuests);
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [darkMode, setDarkMode] = useState(true);
  const [showConfetti, setShowConfetti] = useState(false);
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const [leaderboard, setLeaderboard] = useState<User[]>([]);
  const [adminUsers, setAdminUsers] = useState<User[]>([]);
  const [adminError, setAdminError] = useState<string | null>(null);
  const [showCreateTask, setShowCreateTask] = useState(false);
  const [showTaskForm, setShowTaskForm] = useState(false);
  const [taskFormMode, setTaskFormMode] = useState<"create" | "edit">("create");
  const [editingTaskId, setEditingTaskId] = useState<string | null>(null);
  const sidebarWrapRef = useRef<HTMLDivElement | null>(null);
  const [sidebarWidth, setSidebarWidth] = useState(256); // default 64 * 4 = 256px

useLayoutEffect(() => {
  const el = sidebarWrapRef.current;
  if (!el) return;

  const ro = new ResizeObserver(() => {
    const w = el.getBoundingClientRect().width;
    setSidebarWidth(Math.round(w));
  });

  ro.observe(el);

  // set initial width immediately
  setSidebarWidth(Math.round(el.getBoundingClientRect().width));

  return () => ro.disconnect();
}, []);




  // Apply dark mode
  useEffect(() => {
    if (darkMode) document.documentElement.classList.add('dark');
    else document.documentElement.classList.remove('dark');
  }, [darkMode]);

  // ✅ Auto-login on refresh using REAL profile stats
  useEffect(() => {
    (async () => {
      try {
        const u = await fetchMeProfile();
        setUser(u);
      } catch {
        // not logged in - ignore
      }
    })();
  }, []);

  // Helper: refresh tasks from backend
  const refreshTasks = async () => {
    const data = await apiTasks();
    setTasks(data);
  };

  // Whenever user becomes non-null, load tasks
  useEffect(() => {
  if (!user) return;

  (async () => {
    try {
      await refreshTasks();
      await refreshLeaderboard();
      await refreshAchievements();

      if (user.role === "admin") await refreshAdminUsers();
    } catch (e) {
      console.error(e);
    }
  })();
}, [user]);


useEffect(() => {
  if (!user) return;
  if (user.role !== "admin") return;
  if (currentScreen !== "admin") return;

  (async () => {
    try {
      setAdminError(null);
      await refreshAdminUsers();
    } catch (e: any) {
      console.error(e);
      setAdminUsers([]);
      setAdminError(e?.message || "Failed to load admin users");
    }
  })();
}, [user, currentScreen]);


  // ✅ LoginScreen already logged in + cookie is set → just fetch profile
  const handleLogin = async (_loggedInUser: User) => {
    const u = await fetchMeProfile();
    setUser(u);
  };

  const handleLogout = () => {
    setUser(null);
    setTasks([]);
    setCurrentScreen('dashboard');
  };

  const handleCompleteTask = async (taskId: string) => {
  try {
    const res = await apiCompleteTask(taskId); // ✅ capture response
    await refreshTasks();

    // ✅ instant achievements update (if backend returns achievements)
    if (res?.achievements) {
      const unlocked: string[] = Array.isArray(res.achievements)
        ? res.achievements
        : Object.values(res.achievements);

      applyUnlockedAchievements(unlocked);
    }

    setQuests((prev) =>
      prev.map((quest) => ({
        ...quest,
        progress: quest.progress + 1,
      }))
    );

    setShowConfetti(true);

   // OPTIONAL: if you want streak/points to update instantly:
    const u = await fetchMeProfile();
    setUser(u);

  } catch (e) {
    console.error(e);
  }
};


  const handleViewTask = (taskId: string) => {
    const task = tasks.find((t) => t.id === taskId);
    if (task) setSelectedTask(task);
  };

  const handleEditTask = (taskId: string) => {
    setTaskFormMode("edit");
    setEditingTaskId(taskId);
    setShowTaskForm(true);
  };


  const handleCreateTask = () => {
    setTaskFormMode("create");
    setEditingTaskId(null);
    setShowTaskForm(true);
  };

  const applyUnlockedAchievements = (unlockedNames: string[]) => {
  setAchievements((prev) =>
    prev.map((a) => {
      const isUnlocked = unlockedNames.some(
        (n) => n.toLowerCase() === a.name.toLowerCase()
      );

      if (!isUnlocked || a.unlocked) return a;

      return {
        ...a,
        unlocked: true,
        unlockedAt: new Date().toISOString(),
      };
    })
  );
};


const refreshAchievements = async () => {
  const unlocked = await apiAchievements();
  applyUnlockedAchievements(unlocked);
};



  const toApiType = (t: Task["type"]) =>
  t === "daily" ? "DAILY" :
  t === "weekly" ? "WEEKLY" :
  t === "monthly" ? "MONTHLY" :
  "ONE_TIME";

const toApiDifficulty = (d: Task["difficulty"]) =>
  d === "easy" ? "EASY" : d === "medium" ? "MEDIUM" : "HARD";

const toApiCategory = (c: Task["category"]) =>
  c === "work" ? "WORK" :
  c === "study" ? "STUDY" :
  c === "health" ? "HEALTH" :
  c === "personal" ? "PERSONAL" :
  "OTHER";

const toYmd = (isoOrYmd: string | null | undefined) => {
  if (!isoOrYmd) return "";
  // if it's already YYYY-MM-DD
  if (/^\d{4}-\d{2}-\d{2}$/.test(isoOrYmd)) return isoOrYmd;
  // ISO -> YYYY-MM-DD
  const d = new Date(isoOrYmd);
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}`;
};

const handleSubmitTaskForm = async (values: {
  title: string;
  description: string;
  type: "DAILY" | "WEEKLY" | "MONTHLY" | "ONE_TIME";
  difficulty: "EASY" | "MEDIUM" | "HARD";
  category: "WORK" | "STUDY" | "HEALTH" | "PERSONAL" | "OTHER";
  dueDate: string | null;
}) => {
  if (taskFormMode === "create") {
    await apiCreateTask(values);
  } else {
    if (!editingTaskId) throw new Error("No task selected for edit");
    await apiUpdateTask(editingTaskId, values);
  }
  await refreshTasks();
};

  


  const handleSubmitCreateTask = async (req: {
    title: string;
    description: string;
    type: "DAILY" | "WEEKLY" | "MONTHLY" | "ONE_TIME";
    difficulty: "EASY" | "MEDIUM" | "HARD";
    category: "WORK" | "STUDY" | "HEALTH" | "PERSONAL" | "OTHER";
    dueDate: string | null;
  }) => {
    await apiCreateTask(req);
    await refreshTasks();
  };



const refreshAdminUsers = async () => setAdminUsers(await apiAdminUsers());
const refreshLeaderboard = async () => setLeaderboard(await apiLeaderboard());


const handleAdminCreateUser = async (username: string, role: "admin" | "player", password: string) => {
  await apiAdminCreateUser(
    username,
    role === "admin" ? "ADMIN" : "PLAYER",
    password
  );


  await refreshLeaderboard();
  await refreshAdminUsers();
};


const handleAdminResetStats = async (userId: string) => {
  await apiAdminResetUser(userId);
  await refreshAdminUsers();
  await refreshLeaderboard();
};

const handleAdminDeleteUser = async (userId: string) => {
  await apiAdminDeleteUser(userId);
  await refreshAdminUsers();
  await refreshLeaderboard();
};

const handleAdminSetRole = async (userId: string, role: "admin" | "player") => {
  await apiAdminSetRole(userId, role === "admin" ? "ADMIN" : "PLAYER");
  await refreshLeaderboard();
  await refreshAdminUsers();
};



  if (!user) {
    return <LoginScreen onLogin={handleLogin} />;
  }

  return (
    <div className="min-h-screen bg-[rgb(var(--color-background))]">
      <div className="hidden md:block" ref={sidebarWrapRef}>
        <Sidebar
          currentScreen={currentScreen}
          onNavigate={(screen) => setCurrentScreen(screen as Screen)}
          user={user}
          collapsed={sidebarCollapsed}
          onToggleCollapse={() => setSidebarCollapsed(!sidebarCollapsed)}
        />
      </div>


      <div
        className={`transition-all duration-300 pt-16 ${sidebarCollapsed ? 'md:ml-20' : 'md:ml-64'}`}
      >


        <Header
          user={user}
          onLogout={handleLogout}
          darkMode={darkMode}
          onToggleDarkMode={() => setDarkMode(!darkMode)}
          sidebarCollapsed={sidebarCollapsed}   
        />



        <main className="p-4 md:p-6 min-h-screen pb-20 md:pb-6">
          {currentScreen === 'dashboard' && (
            <DashboardScreen
              user={user}
              tasks={tasks}
              achievements={achievements}
              quests={quests}
              onCompleteTask={handleCompleteTask}
              onViewTask={handleViewTask}
            />
          )}

          {currentScreen === 'tasks' && (
            <TasksScreen
              tasks={tasks}
              onCompleteTask={handleCompleteTask}
              onEditTask={handleEditTask}
              onViewTask={handleViewTask}
              onCreateTask={handleCreateTask}
            />
          )}

          {currentScreen === 'calendar' && (
            <CalendarScreen
              tasks={tasks}
              onCompleteTask={handleCompleteTask}
              onViewTask={handleViewTask}
            />
          )}

          {currentScreen === 'quests' && (
            <QuestsScreen
              quests={quests}
              tasks={tasks}
              onCompleteTask={handleCompleteTask}
              onViewTask={handleViewTask}
            />
          )}

          {currentScreen === 'achievements' && <AchievementsScreen achievements={achievements} />}

          {currentScreen === 'leaderboard' && (
            <LeaderboardScreen leaderboard={leaderboard} currentUser={user} />
          )}

          {currentScreen === 'shop' && <ShopScreen userPoints={user.totalPoints} />}

          {currentScreen === 'admin' && user.role === 'admin' && (
            <AdminScreen
              users={adminUsers}
              onCreateUser={handleAdminCreateUser}
              onResetStats={handleAdminResetStats}
              onDeleteUser={handleAdminDeleteUser}
              onSetRole={handleAdminSetRole}
            />

          )}


          {currentScreen === 'settings' && (
            <SettingsScreen user={user} darkMode={darkMode} onToggleDarkMode={() => setDarkMode(!darkMode)} />
          )}
        </main>
      </div>

      <nav className="md:hidden fixed bottom-0 left-0 right-0 bg-[rgb(var(--color-surface))] border-t border-[rgb(var(--color-border))] z-30">
        <div className="flex items-center justify-around px-2 py-3">
          {[
            { id: 'dashboard', icon: LayoutDashboard, label: 'Home' },
            { id: 'tasks', icon: CheckSquare, label: 'Tasks' },
            { id: 'calendar', icon: Calendar, label: 'Calendar' },
            { id: 'quests', icon: Target, label: 'Quests' },
            { id: 'settings', icon: Settings, label: 'More' },
          ].map((item) => {
            const Icon = item.icon;
            const isActive = currentScreen === item.id;

            return (
              <button
                key={item.id}
                onClick={() => setCurrentScreen(item.id as Screen)}
                className={`flex flex-col items-center gap-1 px-3 py-2 rounded-lg transition-all ${
                  isActive ? 'text-violet-400' : 'text-[rgb(var(--color-text-tertiary))]'
                }`}
              >
                <Icon size={20} />
                <span className="text-xs">{item.label}</span>
              </button>
            );
          })}
        </div>
      </nav>

      {selectedTask && (
        <TaskDetailsModal
          task={selectedTask}
          onClose={() => setSelectedTask(null)}
          onComplete={handleCompleteTask}
          onEdit={handleEditTask}
        />
      )}

      {showCreateTask && (
        <CreateTaskModal
          onClose={() => setShowCreateTask(false)}
          onCreate={handleSubmitCreateTask}
        />
      )}

      {showTaskForm && (
  <TaskFormModal
    mode={taskFormMode}
    onClose={() => setShowTaskForm(false)}
    onSubmit={handleSubmitTaskForm}
    initial={
      taskFormMode === "edit" && editingTaskId
        ? (() => {
            const t = tasks.find((x) => x.id === editingTaskId);
            if (!t) return {};
            return {
              title: t.title,
              description: t.description,
              type: toApiType(t.type),
              difficulty: toApiDifficulty(t.difficulty),
              category: toApiCategory(t.category),
              dueDate: t.type === "one-time" ? toYmd(t.dueDate) : "",
            };
          })()
        : {}
    }
  />
)}



      <Confetti show={showConfetti} onComplete={() => setShowConfetti(false)} />
    </div>
  );
}
