package com.upthink.qms.domain;

public enum Group {
    SUPER_ADMIN(5),
    MANAGER(4),
    QM_ADMIN(3),
    QM_AUTHOR(2),
    USER(1);

    private final int rank;

    Group(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    public boolean isHigherOrEqual(Group other) {
        return this.rank >= other.getRank();
    }
}
