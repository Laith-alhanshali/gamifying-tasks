package org.laith.domain.model;

import java.util.List;

public class Rank {

    private final String name;
    private final int minPoints;

    public Rank(String name, int minPoints) {
        this.name = name;
        this.minPoints = minPoints;
    }

    public String getName() {
        return name;
    }

    public int getMinPoints() {
        return minPoints;
    }

    @Override
    public String toString() {
        return "Rank{name='" + name + "', minPoints=" + minPoints + "}";
    }

    public static Rank getRankForPoints(int points, List<Rank> ranks) {
        Rank result = null;
        for (Rank rank : ranks) {
            if (points >= rank.getMinPoints()) {
                result = rank;
            } else {
                break;
            }
        }
        return result;
    }
}
