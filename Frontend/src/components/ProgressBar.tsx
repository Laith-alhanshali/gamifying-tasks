import React from 'react';

interface ProgressBarProps {
  progress: number;
  color?: 'primary' | 'success' | 'warning' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  showLabel?: boolean;
}

export const ProgressBar: React.FC<ProgressBarProps> = ({
  progress,
  color = 'primary',
  size = 'md',
  showLabel = false,
}) => {
  const colorStyles = {
    primary: {
      gradient: 'from-violet-500 via-purple-500 to-blue-500',
      glow: 'shadow-violet-500/50',
    },
    success: {
      gradient: 'from-emerald-500 via-green-500 to-teal-500',
      glow: 'shadow-emerald-500/50',
    },
    warning: {
      gradient: 'from-amber-500 via-yellow-500 to-orange-500',
      glow: 'shadow-amber-500/50',
    },
    danger: {
      gradient: 'from-pink-500 via-rose-500 to-red-500',
      glow: 'shadow-pink-500/50',
    },
  };

  const sizes = {
    sm: 'h-2',
    md: 'h-3',
    lg: 'h-5',
  };

  const style = colorStyles[color];
  const clampedProgress = Math.min(progress, 100);

  return (
    <div className="w-full">
      <div className={`w-full bg-[rgb(var(--color-surface-elevated))] rounded-full overflow-hidden ${sizes[size]} border border-[rgb(var(--color-border))] relative`}>
        {/* Progress bar with gradient and glow */}
        <div
          className={`bg-gradient-to-r ${style.gradient} ${sizes[size]} rounded-full transition-all duration-500 ease-out relative overflow-hidden`}
          style={{ width: `${clampedProgress}%` }}
        >
          {/* Animated shine effect */}
          <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/40 to-transparent animate-pulse" />
          
          {/* Glow effect */}
          <div className={`absolute inset-0 ${style.glow} blur-sm`} />
        </div>
        
        {/* Tick marks for gamey feel */}
        {size !== 'sm' && (
          <div className="absolute inset-0 flex items-center">
            {[25, 50, 75].map((tick) => (
              <div
                key={tick}
                className="absolute w-px h-full bg-[rgb(var(--color-border))] opacity-30"
                style={{ left: `${tick}%` }}
              />
            ))}
          </div>
        )}
      </div>
      {showLabel && (
        <div className="text-sm font-bold text-[rgb(var(--color-text-secondary))] mt-2 text-right">
          {Math.round(clampedProgress)}%
        </div>
      )}
    </div>
  );
};
