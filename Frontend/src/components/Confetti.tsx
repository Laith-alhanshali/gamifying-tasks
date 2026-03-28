import React, { useEffect, useState } from 'react';
import { motion, AnimatePresence } from 'motion/react';

interface ConfettiProps {
  show: boolean;
  onComplete?: () => void;
}

export const Confetti: React.FC<ConfettiProps> = ({ show, onComplete }) => {
  const [particles, setParticles] = useState<Array<{ id: number; x: number; color: string }>>([]);

  useEffect(() => {
    if (show) {
      const colors = ['#7c3aed', '#3b82f6', '#22c55e', '#f59e0b', '#ec4899'];
      const newParticles = Array.from({ length: 30 }, (_, i) => ({
        id: i,
        x: Math.random() * 100,
        color: colors[Math.floor(Math.random() * colors.length)],
      }));
      setParticles(newParticles);

      const timer = setTimeout(() => {
        setParticles([]);
        onComplete?.();
      }, 2000);

      return () => clearTimeout(timer);
    }
  }, [show, onComplete]);

  return (
    <AnimatePresence>
      {show && (
        <div className="fixed inset-0 pointer-events-none z-50 overflow-hidden">
          {particles.map((particle) => (
            <motion.div
              key={particle.id}
              className="absolute w-2 h-2 rounded-full"
              style={{
                left: `${particle.x}%`,
                top: '-10px',
                backgroundColor: particle.color,
              }}
              initial={{ y: 0, opacity: 1, rotate: 0 }}
              animate={{
                y: window.innerHeight + 50,
                opacity: 0,
                rotate: Math.random() * 720 - 360,
              }}
              transition={{
                duration: 2,
                ease: 'easeOut',
              }}
            />
          ))}
        </div>
      )}
    </AnimatePresence>
  );
};
