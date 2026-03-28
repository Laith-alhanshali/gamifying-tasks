import React from 'react';

interface CardProps {
  children: React.ReactNode;
  className?: string;
  hover?: boolean;
  glass?: boolean;
}

export const Card: React.FC<CardProps> = ({ children, className = '', hover = false, glass = false }) => {
  const baseStyles = 'rounded-xl border transition-all duration-200';
  const hoverStyles = hover ? 'hover:shadow-lg hover:shadow-violet-500/10 hover:-translate-y-0.5 cursor-pointer' : '';
  const glassStyles = glass ? 'glass' : 'bg-[rgb(var(--color-surface))]';

  return (
    <div className={`${baseStyles} ${hoverStyles} ${glassStyles} ${className}`}>
      {children}
    </div>
  );
};
