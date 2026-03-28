import React, { useEffect, useState } from "react";
import { Card } from "./Card";
import { Button } from "./Button";
import { X } from "lucide-react";

export type TaskFormValues = {
  title: string;
  description: string;
  type: "DAILY" | "WEEKLY" | "MONTHLY" | "ONE_TIME";
  difficulty: "EASY" | "MEDIUM" | "HARD";
  category: "WORK" | "STUDY" | "HEALTH" | "PERSONAL" | "OTHER";
  dueDate: string | null; // YYYY-MM-DD
};

type Props = {
  mode: "create" | "edit";
  initial?: Partial<TaskFormValues>;
  onClose: () => void;
  onSubmit: (values: TaskFormValues) => Promise<void>;
};

export const TaskFormModal: React.FC<Props> = ({ mode, initial, onClose, onSubmit }) => {
  const [title, setTitle] = useState(initial?.title ?? "");
  const [description, setDescription] = useState(initial?.description ?? "");
  const [type, setType] = useState<TaskFormValues["type"]>(initial?.type ?? "ONE_TIME");
  const [difficulty, setDifficulty] = useState<TaskFormValues["difficulty"]>(initial?.difficulty ?? "EASY");
  const [category, setCategory] = useState<TaskFormValues["category"]>(initial?.category ?? "OTHER");
  const [dueDate, setDueDate] = useState<string>(initial?.dueDate ?? "");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // If initial changes (switching which task is being edited), update fields
  useEffect(() => {
    setTitle(initial?.title ?? "");
    setDescription(initial?.description ?? "");
    setType(initial?.type ?? "ONE_TIME");
    setDifficulty(initial?.difficulty ?? "EASY");
    setCategory(initial?.category ?? "OTHER");
    setDueDate(initial?.dueDate ?? "");
  }, [initial]);

  const submit = async () => {
    const t = title.trim();
    const d = description.trim();
    if (!t) return setError("Title is required");
    if (!d) return setError("Description is required");

    setError(null);
    setLoading(true);
    try {
      await onSubmit({
        title: t,
        description: d,
        type,
        difficulty,
        category,
        dueDate: dueDate.trim() ? dueDate.trim() : null,
      });
      onClose();
    } catch (e: any) {
      setError(e?.message || "Failed to save task");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <Card className="max-w-lg w-full p-6">
        <div className="flex items-center justify-between mb-4">
          <h3>{mode === "create" ? "Create New Task" : "Edit Quest"}</h3>
          <button onClick={onClose} className="text-[rgb(var(--color-text-tertiary))] hover:text-[rgb(var(--color-text-primary))]">
            <X size={20} />
          </button>
        </div>

        {error && (
          <Card className="p-3 mb-4 border border-red-500/30 bg-red-500/10 text-red-300">
            {error}
          </Card>
        )}

        <div className="space-y-4">
          <div>
            <label className="block text-sm mb-2 text-[rgb(var(--color-text-secondary))]">Title</label>
            <input
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="w-full px-4 py-2 bg-[rgb(var(--color-surface-elevated))] border border-[rgb(var(--color-border))] rounded-lg"
              placeholder="e.g., Morning workout"
              autoFocus
            />
          </div>

          <div>
            <label className="block text-sm mb-2 text-[rgb(var(--color-text-secondary))]">Description</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full px-4 py-2 bg-[rgb(var(--color-surface-elevated))] border border-[rgb(var(--color-border))] rounded-lg min-h-[90px]"
              placeholder="What needs to be done?"
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div>
              <label className="block text-sm mb-2 text-[rgb(var(--color-text-secondary))]">Type</label>
              <select
                value={type}
                onChange={(e) => setType(e.target.value as any)}
                className="w-full px-4 py-2 bg-[rgb(var(--color-surface-elevated))] border border-[rgb(var(--color-border))] rounded-lg"
              >
                <option value="ONE_TIME">One-time</option>
                <option value="DAILY">Daily</option>
                <option value="WEEKLY">Weekly</option>
                <option value="MONTHLY">Monthly</option>
              </select>
              <p className="text-xs mt-1 text-[rgb(var(--color-text-tertiary))]">
                Note: for Daily/Weekly/Monthly, backend will auto-set the due date.
              </p>
            </div>

            <div>
              <label className="block text-sm mb-2 text-[rgb(var(--color-text-secondary))]">Difficulty</label>
              <select
                value={difficulty}
                onChange={(e) => setDifficulty(e.target.value as any)}
                className="w-full px-4 py-2 bg-[rgb(var(--color-surface-elevated))] border border-[rgb(var(--color-border))] rounded-lg"
              >
                <option value="EASY">Easy</option>
                <option value="MEDIUM">Medium</option>
                <option value="HARD">Hard</option>
              </select>
            </div>

            <div>
              <label className="block text-sm mb-2 text-[rgb(var(--color-text-secondary))]">Category</label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value as any)}
                className="w-full px-4 py-2 bg-[rgb(var(--color-surface-elevated))] border border-[rgb(var(--color-border))] rounded-lg"
              >
                <option value="WORK">Work</option>
                <option value="STUDY">Study</option>
                <option value="HEALTH">Health</option>
                <option value="PERSONAL">Personal</option>
                <option value="OTHER">Other</option>
              </select>
            </div>

            <div>
              <label className="block text-sm mb-2 text-[rgb(var(--color-text-secondary))]">Due Date</label>
              <input
                type="date"
                value={dueDate}
                onChange={(e) => setDueDate(e.target.value)}
                disabled={type !== "ONE_TIME"} // optional (prevents confusion)
                className="w-full px-4 py-2 bg-[rgb(var(--color-surface-elevated))] border border-[rgb(var(--color-border))] rounded-lg disabled:opacity-50"
              />
            </div>
          </div>
        </div>

        <div className="flex items-center gap-3 mt-6">
          <Button variant="secondary" className="flex-1" onClick={onClose} disabled={loading}>
            Cancel
          </Button>
          <Button className="flex-1" onClick={submit} disabled={loading}>
            {loading ? "Saving..." : mode === "create" ? "Create" : "Save"}
          </Button>
        </div>
      </Card>
    </div>
  );
};
