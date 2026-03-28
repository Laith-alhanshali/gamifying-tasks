import React from 'react';
import { Sparkles, Star, Zap } from 'lucide-react';

export const FloatingParticles: React.FC = () => {
  return (
    <div className="fixed inset-0 pointer-events-none overflow-hidden z-0 opacity-20">
      {/* Floating sparkles */}
      <div className="absolute top-20 left-10 animate-pulse">
        <Sparkles size={16} className="text-violet-400" />
      </div>
      <div className="absolute top-40 right-20 animate-pulse delay-100" style={{ animationDelay: '1s' }}>
        <Star size={14} className="text-blue-400" />
      </div>
      <div className="absolute top-60 left-1/4 animate-pulse delay-200" style={{ animationDelay: '2s' }}>
        <Zap size={12} className="text-amber-400" />
      </div>
      <div className="absolute bottom-40 right-1/3 animate-pulse" style={{ animationDelay: '0.5s' }}>
        <Sparkles size={18} className="text-purple-400" />
      </div>
      <div className="absolute bottom-20 left-1/2 animate-pulse" style={{ animationDelay: '1.5s' }}>
        <Star size={16} className="text-cyan-400" />
      </div>
    </div>
  );
};
