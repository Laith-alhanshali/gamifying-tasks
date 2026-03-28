import React from 'react';
import { Card } from './Card';
import { LucideIcon } from 'lucide-react';

interface StatCardProps {
  title: string;
  value: string | number;
  icon: LucideIcon;
  color?: 'primary' | 'success' | 'warning' | 'danger';
  subtitle?: string;
}

export const StatCard: React.FC<StatCardProps> = ({ title, value, icon: Icon, color = 'primary', subtitle }) => {
  const colorStyles = {
    primary: {
      gradient: 'from-violet-500 to-blue-500',
      glow: 'shadow-violet-500/50',
      text: 'text-violet-400',
    },
    success: {
      gradient: 'from-emerald-500 to-green-500',
      glow: 'shadow-emerald-500/50',
      text: 'text-emerald-400',
    },
    warning: {
      gradient: 'from-amber-500 to-orange-500',
      glow: 'shadow-amber-500/50',
      text: 'text-amber-400',
    },
    danger: {
      gradient: 'from-pink-500 to-rose-500',
      glow: 'shadow-pink-500/50',
      text: 'text-pink-400',
    },
  };

  const style = colorStyles[color];

  return (
    <Card className="p-6 gaming-card hover:scale-105 transition-transform duration-300" hover>
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <p className="text-sm text-[rgb(var(--color-text-tertiary))] mb-2 uppercase tracking-wide font-medium">{title}</p>
          <h3 className="text-3xl font-bold mb-1 bg-gradient-to-r from-violet-400 to-blue-400 bg-clip-text text-transparent">{value}</h3>
          {subtitle && <p className="text-xs text-[rgb(var(--color-text-tertiary))] font-medium">{subtitle}</p>}
        </div>
        <div 
          className={`w-14 h-14 bg-gradient-to-br ${style.gradient} rounded-xl flex items-center justify-center shadow-xl ${style.glow} relative overflow-hidden`}
        >
          <Icon size={28} className="text-white relative z-10" strokeWidth={2.5} />
          {/* Shine effect */}
          <div className="absolute inset-0 bg-gradient-to-tr from-transparent via-white/20 to-transparent animate-pulse" />
        </div>
      </div>
    </Card>
  );
};
