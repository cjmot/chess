package dataaccess;

public class DatabaseManager {
    private final MemoryUserAccess userAccess;
    private final MemoryGameAccess gameAccess;
    private final MemoryAuthAccess authAccess;

    public DatabaseManager() {
        userAccess = new MemoryUserAccess();
        gameAccess = new MemoryGameAccess();
        authAccess = new MemoryAuthAccess();
    }

    public MemoryUserAccess userAccess() {
        return this.userAccess;
    }

    public MemoryGameAccess gameAccess() {
        return this.gameAccess;
    }

    public MemoryAuthAccess authAccess() {
        return this.authAccess;
    }
}
