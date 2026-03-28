package org.laith;

import org.junit.jupiter.api.Test;
import org.laith.domain.model.Rank;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RankTest {

    @Test
    void constructor_setsNameAndMinPoints() {
        Rank r = new Rank("Bronze", 0);
        assertEquals("Bronze", r.getName());
        assertEquals(0, r.getMinPoints());
        assertNotNull(r.toString());
    }

    @Test
    void getRankForPoints_selectsCorrectRank() {
        List<Rank> ranks = List.of(
                new Rank("Bronze", 0),
                new Rank("Silver", 100),
                new Rank("Gold", 300)
        );

        assertEquals("Bronze", Rank.getRankForPoints(0, ranks).getName());
        assertEquals("Bronze", Rank.getRankForPoints(99, ranks).getName());
        assertEquals("Silver", Rank.getRankForPoints(100, ranks).getName());
        assertEquals("Silver", Rank.getRankForPoints(299, ranks).getName());
        assertEquals("Gold", Rank.getRankForPoints(300, ranks).getName());
        assertEquals("Gold", Rank.getRankForPoints(999, ranks).getName());
    }
}
