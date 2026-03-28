import React from 'react';
import { Rank } from '../types';
import { Trophy, Shield } from 'lucide-react';
import { getRankColor } from '../utils/helpers';

interface RankBadgeProps {
  rank: Rank;
  size?: 'sm' | 'md' | 'lg';
  showLabel?: boolean;
}

export const RankBadge: React.FC<RankBadgeProps> = ({ rank, size = 'md', showLabel = true }) => {
  const sizes = {
    sm: 'w-8 h-8 text-xs',
    md: 'w-12 h-12 text-sm',
    lg: 'w-20 h-20 text-lg',
  };

  const iconSizes = {
    sm: 16,
    md: 24,
    lg: 40,
  };

  const gradients = {
    bronze: 'from-orange-600 via-orange-500 to-amber-600',
    silver: 'from-gray-300 via-gray-200 to-gray-400',
    gold: 'from-yellow-400 via-yellow-300 to-amber-500',
    platinum: 'from-slate-300 via-gray-100 to-slate-400',
    diamond: 'from-cyan-300 via-blue-300 to-cyan-500',
  };

  const glows = {
    bronze: 'shadow-orange-500/60',
    silver: 'shadow-gray-400/60',
    gold: 'shadow-yellow-400/80',
    platinum: 'shadow-slate-300/60',
    diamond: 'shadow-cyan-400/80',
  };

  const labels = {
    bronze: 'Bronze',
    silver: 'Silver',
    gold: 'Gold',
    platinum: 'Platinum',
    diamond: 'Diamond',
  };

  return (
    <div className="flex items-center gap-3">
      <div className="relative">
        <div 
          className={`${sizes[size]} bg-gradient-to-br ${gradients[rank]} rounded-xl flex items-center justify-center shadow-2xl ${glows[rank]} border-2 border-white/30 relative overflow-hidden`}
        >
          {/* Inner glow */}
          <div className="absolute inset-0 bg-gradient-to-t from-transparent via-white/30 to-transparent" />
          
          {/* Icon */}
          <Shield size={iconSizes[size]} className="text-white relative z-10 drop-shadow-lg" fill="white" fillOpacity={0.2} />
          
          {/* Sparkle overlay */}
          <div className="absolute inset-0 bg-gradient-to-tr from-transparent via-white/40 to-transparent opacity-50" />
        </div>
        
        {/* Outer glow ring for higher ranks */}
        {(rank === 'gold' || rank === 'platinum' || rank === 'diamond') && (
          <div className={`absolute inset-0 rounded-xl blur-md bg-gradient-to-br ${gradients[rank]} opacity-50 -z-10`} />
        )}
      </div>
      
      {showLabel && (
        <div className="flex flex-col">
          <span className="font-bold text-lg" style={{ color: getRankColor(rank) }}>
            {labels[rank]}
          </span>
          <span className="text-xs text-[rgb(var(--color-text-tertiary))] uppercase tracking-wider">
            Rank
          </span>
        </div>
      )}
    </div>
  );
};
