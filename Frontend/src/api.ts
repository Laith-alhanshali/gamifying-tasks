// src/api.ts
import { Task, TaskCategory, TaskDifficulty, TaskType, TaskStatus, User } from "./types";

export type ApiTask = {
  taskId: number;
  title: string;
  description: string;
  type: "DAILY" | "WEEKLY" | "MONTHLY" | "ONE_TIME";
  difficulty: "EASY" | "MEDIUM" | "HARD";
  category: "WORK" | "STUDY" | "HEALTH" | "PERSONAL" | "OTHER";
  completed: boolean;
  dueDate: string | null;         // "YYYY-MM-DD"
  createdAt: string | null;       // ISO datetime
  completedAt: string | null;     // ISO datetime
};

export type ApiMe = {
  authenticated: boolean;
  userId: number;
  username: string;
  role: "ADMIN" | "PLAYER";
};

export type MeProfile = {
  authenticated: boolean;
  userId: number;
  username: string;
  role: "ADMIN" | "PLAYER";
  totalPoints: number;
  xp: number;
  level: number;
  currentStreak: number;
  longestStreak: number;
  rank: string; // "Bronze" etc
  tasksCompleted: number;
  achievementsCount: number;
};

export async function apiMeProfile(): Promise<MeProfile> {
  const r = await fetch("/me/profile", { credentials: "include" });
  if (!r.ok) throw new Error(await r.text());
  return r.json();
}

export function mapProfileToUser(p: MeProfile): User {
  const rankLower =
    (p.rank || "Bronze").toLowerCase() as User["rank"]; // bronze/silver/...

  return {
    id: String(p.userId),
    username: p.username,
    role: p.role === "ADMIN" ? "admin" : "player",
    level: p.level,
    xp: p.totalPoints,           // ✅ xp = totalPoints
    rank: rankLower,
    currentStreak: p.currentStreak,
    longestStreak: p.longestStreak,
    totalPoints: p.totalPoints,
    tasksCompleted: p.tasksCompleted,
  };
}

export type LeaderboardRow = {
  userId: number;
  username: string;
  role: "ADMIN" | "PLAYER";
  totalPoints: number;
  xp: number;
  level: number;
  rank: "BRONZE" | "SILVER" | "GOLD" | "PLATINUM" | "DIAMOND";
  currentStreak: number;
  longestStreak: number;
  tasksCompleted: number;
};

function rankToFrontend(rank: string): User["rank"] {
  const r = rank?.toLowerCase();
  if (r === "bronze" || r === "silver" || r === "gold" || r === "platinum" || r === "diamond") return r;
  return "bronze";
}

function rowToUser(r: LeaderboardRow): User {
  return {
    id: String(r.userId),
    username: r.username,
    role: r.role === "ADMIN" ? "admin" : "player",
    level: r.level,
    xp: r.xp, // ✅
    rank: rankToFrontend(r.rank),
    currentStreak: r.currentStreak,
    longestStreak: r.longestStreak,
    totalPoints: r.totalPoints,
    tasksCompleted: r.tasksCompleted,
  };
}

export async function apiLeaderboard(): Promise<User[]> {
  const r = await fetch("/leaderboard", { credentials: "include" });
  if (!r.ok) throw new Error(await r.text());
  const data: any[] = await r.json();

  // Map into your frontend User shape
  return data.map((row) => {
    const total = row.totalPoints ?? row.TOTAL_POINTS ?? 0;
    const rankName = row.rankName ?? row.RANK_NAME ?? "BRONZE";

    return {
      id: String(row.userId ?? row.USER_ID),
      username: row.username ?? row.USERNAME,
      role: (row.role ?? row.ROLE) === "ADMIN" ? "admin" : "player",
      level: Math.max(1, Math.floor(total / 100) + 1),
      xp: total,               // ✅ xp == totalPoints
      rank: (rankName || "BRONZE").toLowerCase(),
      currentStreak: row.currentStreak ?? row.CURRENT_STREAK ?? 0,
      longestStreak: row.longestStreak ?? row.LONGEST_STREAK ?? 0,
      totalPoints: total,
      tasksCompleted: row.tasksCompleted ?? 0, // if not provided, safe default
    } as User;
  });
}

export async function apiAdminUsers(): Promise<User[]> {
  const r = await fetch("/admin/users", { credentials: "include" });
  if (!r.ok) throw new Error(await r.text());
  const data: any[] = await r.json();

  return data.map((row) => {
    const total = row.totalPoints ?? row.TOTAL_POINTS ?? 0;
    const rankName = row.rankName ?? row.RANK_NAME ?? "BRONZE";

    return {
      id: String(row.userId ?? row.USER_ID),
      username: row.username ?? row.USERNAME,
      role: (row.role ?? row.ROLE) === "ADMIN" ? "admin" : "player",
      level: Math.max(1, Math.floor(total / 100) + 1),
      xp: total,
      rank: (rankName || "BRONZE").toLowerCase(),
      currentStreak: row.currentStreak ?? row.CURRENT_STREAK ?? 0,
      longestStreak: row.longestStreak ?? row.LONGEST_STREAK ?? 0,
      totalPoints: total,
      tasksCompleted: row.tasksCompleted ?? 0,
    } as User;
  });
}

export type CreateTaskRequest = {
  title: string;
  description: string;
  type?: "DAILY" | "WEEKLY" | "MONTHLY" | "ONE_TIME";
  difficulty?: "EASY" | "MEDIUM" | "HARD";
  category?: "WORK" | "STUDY" | "HEALTH" | "PERSONAL" | "OTHER";
  dueDate?: string | null; // "YYYY-MM-DD"
};

export async function apiCreateTask(req: CreateTaskRequest) {
  const r = await fetch("/tasks", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify(req),
  });
  if (!r.ok) throw new Error(await r.text());
  return r.json(); // returns TaskResponse
}

export type UpdateTaskRequest = {
  title?: string;
  description?: string;
  type?: "DAILY" | "WEEKLY" | "MONTHLY" | "ONE_TIME";
  difficulty?: "EASY" | "MEDIUM" | "HARD";
  category?: "WORK" | "STUDY" | "HEALTH" | "PERSONAL" | "OTHER";
  dueDate?: string | null; // "YYYY-MM-DD" or null
};

export async function apiUpdateTask(taskId: string, req: UpdateTaskRequest) {
  const r = await fetch(`/tasks/${taskId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify(req),
  });
  if (!r.ok) throw new Error(await r.text());
  return r.json();
}

export async function apiAchievements(): Promise<string[]> {
  const r = await fetch("/achievements", { credentials: "include" });
  if (!r.ok) throw new Error(await r.text());
  return r.json();
}


// ---- ADMIN API ----

export type CreateUserRequest = {
  username: string;
  password?: string;           // optional if your backend allows null/blank
  role: "ADMIN" | "PLAYER";
};

export async function apiAdminCreateUser(username: string, role: "ADMIN" | "PLAYER", password: string) {
  const r = await fetch("/admin/users", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify({ username, role, password }),
  });
  if (!r.ok) throw new Error(await r.text());
}


export async function apiAdminResetUser(userId: string): Promise<void> {
  const r = await fetch(`/admin/users/${userId}/reset`, {
    method: "POST",
    credentials: "include",
  });
  if (!r.ok) throw new Error(await r.text());
}

export async function apiAdminDeleteUser(userId: string): Promise<void> {
  const r = await fetch(`/admin/users/${userId}`, {
    method: "DELETE",
    credentials: "include",
  });
  if (!r.ok) throw new Error(await r.text());
}

export async function apiAdminSetRole(userId: string, role: "ADMIN" | "PLAYER") {
  const r = await fetch(`/admin/users/${userId}/role`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify({ role }),
  });
  if (!r.ok) throw new Error(await r.text());
}






const typeMap: Record<ApiTask["type"], TaskType> = {
  DAILY: "daily",
  WEEKLY: "weekly",
  MONTHLY: "monthly",
  ONE_TIME: "one-time",
};

const diffMap: Record<ApiTask["difficulty"], TaskDifficulty> = {
  EASY: "easy",
  MEDIUM: "medium",
  HARD: "hard",
};

const catMap: Record<ApiTask["category"], TaskCategory> = {
  WORK: "work",
  STUDY: "study",
  HEALTH: "health",
  PERSONAL: "personal",
  OTHER: "other",
};

// match your Java Task.calculatePoints()
function calcPoints(type: TaskType, diff: TaskDifficulty) {
  const base =
    type === "daily" ? 10 :
    type === "weekly" ? 30 :
    type === "monthly" ? 80 :
    20; // one-time

  const mult =
    diff === "easy" ? 1.0 :
    diff === "medium" ? 1.5 :
    2.0;

  return Math.round(base * mult);
}

function computeStatus(completed: boolean, dueIso: string | null): TaskStatus {
  if (completed) return "completed";
  if (!dueIso) return "pending";

  const due = new Date(dueIso);
  const now = new Date();
  // overdue if due date is before today (midnight)
  const todayMidnight = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime();
  if (due.getTime() < todayMidnight) return "overdue";

  return "pending";
}

// convert "YYYY-MM-DD" to ISO string that your UI expects
function toIsoDate(dueDate: string | null) {
  return dueDate ? new Date(dueDate + "T00:00:00").toISOString() : new Date().toISOString();
}

export function mapApiTask(t: ApiTask): Task {
  const type = typeMap[t.type];
  const difficulty = diffMap[t.difficulty];
  const category = catMap[t.category];

  const dueIso = t.dueDate ? toIsoDate(t.dueDate) : null;

  return {
    id: String(t.taskId),
    title: t.title,
    description: t.description,
    status: computeStatus(t.completed, dueIso),
    dueDate: dueIso ?? new Date().toISOString(),
    type,
    difficulty,
    category,
    points: calcPoints(type, difficulty),
    createdAt: t.createdAt ?? new Date().toISOString(),
    completedAt: t.completedAt ?? undefined,
    recurring: type !== "one-time",
  };
}

// --- API calls (proxy approach: use relative URLs) ---

export async function apiLogin(username: string, password: string): Promise<void> {
  const r = await fetch("/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify({ username, password }),
  });

  // backend returns "OK" text on success, 401 text on failure
  if (!r.ok) throw new Error(await r.text());
}

export async function apiLogout(): Promise<void> {
  const r = await fetch("/auth/logout", {
    method: "POST",
    credentials: "include",
  });
  if (!r.ok) throw new Error(await r.text());
}

export async function apiMe(): Promise<ApiMe> {
  // IMPORTANT: your backend endpoint is /auth/me (you showed it working)
  const r = await fetch("/me/profile", { credentials: "include" });
  if (!r.ok) throw new Error("Not authenticated");
  return r.json();
}

export async function apiTasks(): Promise<Task[]> {
  const r = await fetch("/tasks", { credentials: "include" });
  if (!r.ok) throw new Error(await r.text());
  const data: ApiTask[] = await r.json();
  return data.map(mapApiTask);
}

export async function apiCompleteTask(taskId: string) {
  const r = await fetch(`/tasks/${taskId}/complete`, {
    method: "POST",
    credentials: "include",
  });
  if (!r.ok) throw new Error(await r.text());
  return r.json(); // contains updated points/streak/rank/etc + nextRecurringTask
}

// You said you have no delete button right now, but leaving this is fine (won't break anything)
export async function apiDeleteTask(taskId: string) {
  const r = await fetch(`/tasks/${taskId}`, {
    method: "DELETE",
    credentials: "include",
  });
  if (!r.ok) throw new Error(await r.text());
  return r.json();
}

export function mapMeToUser(me: { userId: number; username: string; role: "ADMIN" | "PLAYER" }): User {
  return {
    id: String(me.userId),
    username: me.username,
    role: me.role === "ADMIN" ? "admin" : "player",
    // these fields don’t exist yet as endpoints — keep safe defaults for now
    level: 1,
    xp: 0,
    rank: "bronze",
    currentStreak: 0,
    longestStreak: 0,
    totalPoints: 0,
    tasksCompleted: 0,
  };
}
