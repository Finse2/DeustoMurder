package model;

public class PlayerActivity {
    private static final int MAX_INACTIVE_TURNS = 5;
    private static final long MAX_INACTIVE_TIME = 10 * 60 * 1000;

    private int inactiveTurns;
    private long lastActivityTime;

    public PlayerActivity() {
        inactiveTurns = 0;
        lastActivityTime = System.currentTimeMillis();
    }

    public void registerActivity () {
        inactiveTurns = 0;
        lastActivityTime = System.currentTimeMillis();
    }

    public void registerInactiveTurn() {
        inactiveTurns++;
    }

    public int getInactiveTurns() {
        return inactiveTurns;
    }

    public long getLastActivityTime() {
        return lastActivityTime;
    }

    public boolean hasTooManyInactiveTurns() {
        return inactiveTurns >= MAX_INACTIVE_TURNS;
    }

    public boolean hasBeenInactiveTooLong() {
        long currentTime = System.currentTimeMillis();
        return currentTime - lastActivityTime == MAX_INACTIVE_TIME;
    }



}
