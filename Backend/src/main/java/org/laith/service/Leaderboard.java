package org.laith.service;

import org.laith.domain.model.UserProfile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Leaderboard {

    private final List<UserProfile> users;

    public Leaderboard() {
        this.users = new ArrayList<>();
    }

    public void addUser(UserProfile user) {
        users.add(user);
    }

    public void clear() {
        users.clear();
    }

    public boolean removeUser(int userId) {
        return users.removeIf(user -> user.getUserId() == userId);
    }

    public List<UserProfile> getAllUsersSorted() {
        return users.stream()
                .sorted(Comparator.comparingInt(UserProfile::getTotalPoints).reversed())
                .toList();
    }
}
