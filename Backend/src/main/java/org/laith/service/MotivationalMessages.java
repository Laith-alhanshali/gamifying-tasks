package org.laith.service;

import java.util.Random;

public class MotivationalMessages {

    private static final String[] MESSAGES = {
            "Nice! Keep the momentum going! 💪",
            "Another one down. You’re on fire! 🔥",
            "Small steps, big progress. Keep it up! ✅",
            "You’re crushing it today!",
            "Discipline > motivation. And you’ve got both 😎",
            "Your future self is proud of you already.",
            "That’s how habits are built. One task at a time.",
            "Legendary move. Keep stacking those wins!"
    };

    private static final Random RANDOM = new Random();

    public static String randomMessage() {
        int index = RANDOM.nextInt(MESSAGES.length);
        return MESSAGES[index];
    }
}
