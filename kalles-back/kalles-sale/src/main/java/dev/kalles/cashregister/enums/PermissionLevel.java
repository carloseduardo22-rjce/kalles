package dev.kalles.cashregister.enums;

public enum PermissionLevel {

    BASIC(1),
    SUPERVISOR(2),
    MANAGER(3),
    ADMIN(4);

    private final int level;

    PermissionLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean canAuthorize(PermissionLevel other) {
        return this.level > other.level;
    }
}
