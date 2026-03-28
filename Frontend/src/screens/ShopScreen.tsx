import React, { useState } from 'react';
import { Card } from '../components/Card';
import { Button } from '../components/Button';
import { Gift, Sparkles, Zap, Lock } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';

interface ShopScreenProps {
  userPoints: number;
}

export const ShopScreen: React.FC<ShopScreenProps> = ({ userPoints }) => {
  const [openingBox, setOpeningBox] = useState<string | null>(null);
  const [revealedItem, setRevealedItem] = useState<{ name: string; rarity: string } | null>(null);

  const mysteryBoxes = [
    {
      id: 'common',
      name: 'Common Mystery Box',
      cost: 100,
      rarity: 'common',
      color: 'from-gray-400 to-gray-600',
      description: 'Contains common rewards and small XP boosts',
    },
    {
      id: 'rare',
      name: 'Rare Mystery Box',
      cost: 500,
      rarity: 'rare',
      color: 'from-blue-400 to-blue-600',
      description: 'Better rewards with rare achievements and XP multipliers',
    },
    {
      id: 'epic',
      name: 'Epic Mystery Box',
      cost: 1000,
      rarity: 'epic',
      color: 'from-purple-400 to-purple-600',
      description: 'Epic rewards including exclusive titles and major XP boosts',
    },
    {
      id: 'legendary',
      name: 'Legendary Mystery Box',
      cost: 2500,
      rarity: 'legendary',
      color: 'from-yellow-400 to-yellow-600',
      description: 'The ultimate rewards: legendary achievements and massive bonuses',
    },
  ];

  const handleOpenBox = (box: typeof mysteryBoxes[0]) => {
    if (userPoints < box.cost) return;
    
    setOpeningBox(box.id);
    setTimeout(() => {
      setRevealedItem({
        name: `${box.rarity.charAt(0).toUpperCase() + box.rarity.slice(1)} Reward`,
        rarity: box.rarity,
      });
      setOpeningBox(null);
    }, 2000);
  };

  const closeReveal = () => {
    setRevealedItem(null);
  };

  return (
    <div className="space-y-6">
      <div>
        <div className="flex items-center gap-2 mb-1">
          <Gift size={28} className="text-violet-400" />
          <h1>Mystery Shop</h1>
        </div>
        <p className="text-[rgb(var(--color-text-secondary))]">
          Spend your hard-earned points on mystery boxes containing exclusive rewards
        </p>
      </div>

      {/* User Points */}
      <Card className="p-6 gradient-primary bg-opacity-10 border-violet-500/30">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm text-[rgb(var(--color-text-tertiary))] mb-1">Your Points</p>
            <h2 className="gradient-primary text-gradient">{userPoints.toLocaleString()} XP</h2>
          </div>
          <div className="w-16 h-16 gradient-primary rounded-xl flex items-center justify-center shadow-lg shadow-violet-500/50">
            <Zap size={32} className="text-white" />
          </div>
        </div>
      </Card>

      {/* Mystery Boxes Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {mysteryBoxes.map((box) => {
          const canAfford = userPoints >= box.cost;
          const isOpening = openingBox === box.id;

          return (
            <Card
              key={box.id}
              className={`p-6 relative overflow-hidden ${!canAfford ? 'opacity-60' : ''}`}
              hover={canAfford}
            >
              {/* Background Glow */}
              <div className={`absolute top-0 right-0 w-32 h-32 bg-gradient-to-br ${box.color} opacity-20 rounded-full blur-3xl`} />

              {/* Lock Overlay for Unaffordable */}
              {!canAfford && (
                <div className="absolute inset-0 bg-[rgb(var(--color-surface))] bg-opacity-40 backdrop-blur-sm flex items-center justify-center z-10">
                  <Lock size={32} className="text-[rgb(var(--color-text-tertiary))] opacity-50" />
                </div>
              )}

              {/* Box Visual */}
              <motion.div
                className="mb-4"
                animate={isOpening ? { rotateY: 360, scale: [1, 1.2, 1] } : {}}
                transition={{ duration: 2, ease: 'easeInOut' }}
              >
                <div className={`w-full aspect-square bg-gradient-to-br ${box.color} rounded-2xl flex items-center justify-center shadow-2xl`}>
                  <Gift size={64} className="text-white" />
                </div>
              </motion.div>

              {/* Details */}
              <h4 className="mb-2">{box.name}</h4>
              <p className="text-sm text-[rgb(var(--color-text-secondary))] mb-4 min-h-[40px]">
                {box.description}
              </p>

              {/* Cost & Button */}
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-1.5">
                  <Zap size={16} className="text-amber-400" />
                  <span className="text-amber-400">{box.cost} XP</span>
                </div>
                <Button
                  size="sm"
                  disabled={!canAfford || isOpening}
                  onClick={() => handleOpenBox(box)}
                >
                  {isOpening ? (
                    <>
                      <Sparkles size={16} className="animate-spin" />
                      Opening...
                    </>
                  ) : (
                    'Open Box'
                  )}
                </Button>
              </div>
            </Card>
          );
        })}
      </div>

      {/* Reward Reveal Modal */}
      <AnimatePresence>
        {revealedItem && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/70 flex items-center justify-center z-50 p-4"
            onClick={closeReveal}
          >
            <motion.div
              initial={{ scale: 0.5, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.5, opacity: 0 }}
              transition={{ type: 'spring', duration: 0.5 }}
              onClick={(e) => e.stopPropagation()}
            >
              <Card className="max-w-md w-full p-8 text-center">
                <motion.div
                  initial={{ rotateY: 0 }}
                  animate={{ rotateY: 360 }}
                  transition={{ duration: 1, ease: 'easeOut' }}
                  className="mb-6"
                >
                  <div className="w-32 h-32 mx-auto bg-gradient-to-br from-violet-400 to-violet-600 rounded-3xl flex items-center justify-center shadow-2xl shadow-violet-500/50">
                    <Sparkles size={64} className="text-white" />
                  </div>
                </motion.div>
                <h2 className="mb-2">You Received!</h2>
                <h3 className="mb-4 gradient-primary text-gradient">{revealedItem.name}</h3>
                <p className="text-sm text-[rgb(var(--color-text-secondary))] mb-6">
                  This {revealedItem.rarity} reward has been added to your inventory!
                </p>
                <Button onClick={closeReveal} className="w-full">
                  Awesome!
                </Button>
              </Card>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Shop Info */}
      <Card className="p-6 bg-gradient-to-br from-violet-500/10 to-blue-500/10 border-violet-500/20">
        <h3 className="mb-3">How Mystery Boxes Work</h3>
        <div className="space-y-2 text-sm text-[rgb(var(--color-text-secondary))]">
          <p>• Earn XP by completing tasks to unlock mystery boxes</p>
          <p>• Higher rarity boxes contain better rewards and bonuses</p>
          <p>• Rewards can include achievements, XP multipliers, and exclusive titles</p>
          <p>• Check back regularly for limited-time special boxes</p>
        </div>
      </Card>
    </div>
  );
};
