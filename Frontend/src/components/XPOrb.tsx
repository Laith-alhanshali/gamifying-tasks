import React from 'react';
import { Zap } from 'lucide-react';

interface XPOrbProps {
  amount: number;
  size?: 'sm' | 'md' | 'lg';
  showIcon?: boolean;
}

export const XPOrb: React.FC<XPOrbProps> = ({ amount, size = 'md', showIcon = true }) => {
  const sizeClasses = {
    sm: 'text-xs px-2 py-1',
    md: 'text-sm px-3 py-1.5',
    lg: 'text-base px-4 py-2',
  };

  const iconSizes = {
    sm: 12,
    md: 14,
    lg: 16,
  };

  return (
    <div className={`xp-orb ${sizeClasses[size]}`}>
      {showIcon && <Zap size={iconSizes[size]} className="text-white" fill="white" />}
      <span className="font-bold text-white">{amount} XP</span>
    </div>
  );
};
