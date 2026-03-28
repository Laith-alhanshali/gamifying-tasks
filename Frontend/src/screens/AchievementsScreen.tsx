import React, { useState } from 'react';
import { Achievement } from '../types';
import { Card } from '../components/Card';
import { getRarityColor } from '../utils/helpers';
import * as LucideIcons from 'lucide-react';
import { Lock } from 'lucide-react';

interface AchievementsScreenProps {
  achievements: Achievement[];
}

export const AchievementsScreen: React.FC<AchievementsScreenProps> = ({ achievements }) => {
  const [categoryFilter, setCategoryFilter] = useState<string>('all');
  const [rarityFilter, setRarityFilter] = useState<string>('all');

  const categories = ['all', ...Array.from(new Set(achievements.map((a) => a.category)))];
  const rarities = ['all', 'common', 'rare', 'epic', 'legendary'];

  const filteredAchievements = achievements.filter((achievement) => {
    const matchesCategory = categoryFilter === 'all' || achievement.category === categoryFilter;
    const matchesRarity = rarityFilter === 'all' || achievement.rarity === rarityFilter;
    return matchesCategory && matchesRarity;
  });

  const unlockedCount = achievements.filter((a) => a.unlocked).length;
  const totalCount = achievements.length;
  const completionPercentage = Math.round((unlockedCount / totalCount) * 100);

  return (
    <div className="space-y-6">
      <div>
        <h1>Achievements</h1>
        <p className="text-[rgb(var(--color-text-secondary))] mt-1">
          Unlock achievements to showcase your productivity prowess
        </p>
      </div>

      {/* Progress Overview */}
      <Card className="p-6">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h3 className="mb-1">Collection Progress</h3>
            <p className="text-sm text-[rgb(var(--color-text-secondary))]">
              {unlockedCount} of {totalCount} achievements unlocked
            </p>
          </div>
          <div className="text-right">
            <div className="text-3xl gradient-primary text-gradient">{completionPercentage}%</div>
          </div>
        </div>
        <div className="h-3 bg-[rgb(var(--color-surface-elevated))] rounded-full overflow-hidden">
          <div
            className="h-full gradient-primary transition-all duration-500"
            style={{ width: `${completionPercentage}%` }}
          />
        </div>
      </Card>

      {/* Filters */}
      <Card className="p-6">
        <div className="flex flex-col sm:flex-row gap-4">
          <div className="flex-1">
            <label className="block text-sm mb-2 text-[rgb(var(--color-text-secondary))]">Category</label>
            <select
              value={categoryFilter}
              onChange={(e) => setCategoryFilter(e.target.value)}
              className="w-full px-4 py-2 bg-[rgb(var(--color-surface-elevated))] border border-[rgb(var(--color-border))] rounded-lg text-[rgb(var(--color-text-primary))] focus:outline-none focus:ring-2 focus:ring-violet-500"
            >
              {categories.map((cat) => (
                <option key={cat} value={cat}>
                  {cat === 'all' ? 'All Categories' : cat.charAt(0).toUpperCase() + cat.slice(1)}
                </option>
              ))}
            </select>
          </div>
          <div className="flex-1">
            <label className="block text-sm mb-2 text-[rgb(var(--color-text-secondary))]">Rarity</label>
            <select
              value={rarityFilter}
              onChange={(e) => setRarityFilter(e.target.value)}
              className="w-full px-4 py-2 bg-[rgb(var(--color-surface-elevated))] border border-[rgb(var(--color-border))] rounded-lg text-[rgb(var(--color-text-primary))] focus:outline-none focus:ring-2 focus:ring-violet-500"
            >
              {rarities.map((rarity) => (
                <option key={rarity} value={rarity}>
                  {rarity === 'all' ? 'All Rarities' : rarity.charAt(0).toUpperCase() + rarity.slice(1)}
                </option>
              ))}
            </select>
          </div>
        </div>
      </Card>

      {/* Achievements Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filteredAchievements.map((achievement) => {
          const IconComponent =
            (LucideIcons as any)[
              achievement.icon
                .split('-')
                .map((word: string) => word.charAt(0).toUpperCase() + word.slice(1))
                .join('')
            ] || LucideIcons.Award;

          const rarityColor = getRarityColor(achievement.rarity);

          return (
            <Card
              key={achievement.id}
              className={`p-6 relative overflow-hidden ${achievement.unlocked ? 'border-2' : 'opacity-60'}`}
              style={{ borderColor: achievement.unlocked ? rarityColor : undefined }}
              hover
            >
              {/* Background Glow */}
              {achievement.unlocked && (
                <div
                  className="absolute top-0 right-0 w-32 h-32 rounded-full blur-3xl opacity-20"
                  style={{ backgroundColor: rarityColor }}
                />
              )}

              {/* Locked Overlay */}
              {!achievement.unlocked && (
                <div className="absolute inset-0 bg-[rgb(var(--color-surface))] bg-opacity-40 backdrop-blur-sm flex items-center justify-center">
                  <Lock size={48} className="text-[rgb(var(--color-text-tertiary))] opacity-50" />
                </div>
              )}

              {/* Rarity Badge */}
              <div className="flex items-center justify-between mb-4">
                <span
                  className="px-2.5 py-1 rounded-full text-xs capitalize border"
                  style={{
                    color: rarityColor,
                    backgroundColor: `${rarityColor}20`,
                    borderColor: `${rarityColor}30`,
                  }}
                >
                  {achievement.rarity}
                </span>
                {achievement.unlocked && achievement.unlockedAt && (
                  <span className="text-xs text-[rgb(var(--color-text-tertiary))]">
                    {new Date(achievement.unlockedAt).toLocaleDateString()}
                  </span>
                )}
              </div>

              {/* Icon */}
              <div className="flex items-center justify-center mb-4">
                <div
                  className="w-20 h-20 rounded-2xl flex items-center justify-center shadow-lg"
                  style={{ backgroundColor: `${rarityColor}30` }}
                >
                  <IconComponent size={40} style={{ color: rarityColor }} />
                </div>
              </div>

              {/* Details */}
              <div className="text-center">
                <h4 className="mb-2">{achievement.name}</h4>
                <p className="text-sm text-[rgb(var(--color-text-secondary))]">{achievement.description}</p>
              </div>

              {/* Newly Unlocked Badge */}
              {achievement.unlocked &&
                achievement.unlockedAt &&
                new Date(achievement.unlockedAt).getTime() > Date.now() - 86400000 && (
                  <div className="absolute top-2 right-2">
                    <span className="px-2 py-1 bg-green-500 text-white text-xs rounded-full shadow-lg animate-pulse">
                      New!
                    </span>
                  </div>
                )}
            </Card>
          );
        })}
      </div>

      {filteredAchievements.length === 0 && (
        <Card className="p-12">
          <div className="text-center text-[rgb(var(--color-text-tertiary))]">
            <h3 className="mb-2">No achievements found</h3>
            <p className="text-sm">Try adjusting your filters</p>
          </div>
        </Card>
      )}
    </div>
  );
};
