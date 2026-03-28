import React from 'react';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  children: React.ReactNode;
}

export const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  size = 'md',
  children,
  className = '',
  ...props
}) => {
  const baseStyles = 'inline-flex items-center justify-center rounded-lg transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed';
  
  const variants = {
    primary: 'gradient-primary text-white hover:opacity-90 shadow-lg shadow-violet-500/30',
    secondary: 'bg-[rgb(var(--color-surface-elevated))] text-[rgb(var(--color-text-primary))] hover:bg-[rgb(var(--color-border))]',
    ghost: 'bg-transparent text-[rgb(var(--color-text-secondary))] hover:bg-[rgb(var(--color-surface-elevated))]',
    danger: 'bg-[rgb(var(--color-danger))] text-white hover:bg-[rgb(var(--color-danger-light))]',
  };

  const sizes = {
    sm: 'px-3 py-1.5 text-sm gap-1.5',
    md: 'px-4 py-2 text-base gap-2',
    lg: 'px-6 py-3 text-lg gap-2.5',
  };

  return (
    <button
      className={`${baseStyles} ${variants[variant]} ${sizes[size]} ${className}`}
      {...props}
    >
      {children}
    </button>
  );
};
