package org.laith;

import org.junit.jupiter.api.Test;
import org.laith.domain.enums.Role;
import org.laith.domain.model.Rank;
import org.laith.domain.model.UserProfile;
import org.laith.service.Leaderboard;

import static org.junit.jupiter.api.Assertions.*;

public class LeaderboardTest {

    @Test
    void constructor_createsEmptyLeaderboard() {
        Leaderboard lb = new Leaderboard();
        assertNotNull(lb);
        assertTrue(lb.getAllUsersSorted().isEmpty());
    }

    @Test
    void getAllUsersSorted_sortsByPointsDescending() {
        Leaderboard lb = new Leaderboard();

        Rank bronze = new Rank("Bronze", 0);

        UserProfile a = new UserProfile("A", bronze, Role.PLAYER);
        UserProfile b = new UserProfile("B", bronze, Role.PLAYER);

        // Give points by completing tasks (so we're not relying on reflection or changing private fields)
        // We'll just add points by repeating easy one-time tasks.
        // ONE_TIME EASY = 20 points.
        // But completeTask requires rank defs; we can skip and just compare default 0 vs 0 by manual approach:
        // Better: Use loaded constructor to create with points set.
        UserProfile aLoaded = new UserProfile(1, "A", Role.PLAYER, bronze, 40, 0, 0, null, a.getAchievements());
        UserProfile bLoaded = new UserProfile(2, "B", Role.PLAYER, bronze, 100, 0, 0, null, b.getAchievements());

        lb.addUser(aLoaded);
        lb.addUser(bLoaded);

        assertEquals("B", lb.getAllUsersSorted().get(0).getUsername());
        assertEquals("A", lb.getAllUsersSorted().get(1).getUsername());
    }
}
