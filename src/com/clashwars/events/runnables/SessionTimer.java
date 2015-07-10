package com.clashwars.events.runnables;

import com.clashwars.events.Events;
import com.clashwars.events.StartMessages;
import com.clashwars.events.events.GameSession;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Each session has a timer that starts when enough players have joined the session.
 * After the session is started the timer will continue to run every second for the game ending time.
 */
public class SessionTimer extends BukkitRunnable {

    private GameSession session;
    private Long startTime;

    private String[] messages = StartMessages.getMessages();
    private boolean timerRunning = false;
    private int timerSeconds = -1;

    public SessionTimer(GameSession session) {
        this.session = session;
    }


    /** Start the timer if there is no timer running */
    public void startTimer() {
        if (!isTimerRunning()) {
            timerSeconds = 30;
            runTaskTimer(Events.inst(), 0, 20);
        }
    }

    /** Stop the timer if there is a timer running */
    public void stopTimer() {
        if (isTimerRunning()) {
            timerRunning = false;
            timerSeconds = -1;
            cancel();
        }
    }

    /** Sets the time left on the timer. Only if it's lower than the current time left. */
    public void setTimeRemaining(int seconds) {
        timerSeconds = Math.min(timerSeconds, seconds);
    }

    public boolean isTimerRunning() {
        return timerRunning;
    }


    @Override
    public void run() {
        if (timerRunning) {
            if (timerSeconds == 5) {
                //TODO: Calculated randomized game modfiers.
            }

            if (timerSeconds == 0) {
                startTime = System.currentTimeMillis();
                //TODO: Start game.
            } else if (timerSeconds <= 3 && timerSeconds > 0) {
                session.broadcastTitle("&a&l" + timerSeconds, "&7" + messages[timerSeconds - 1], 15, 0, 3, true);
            } else if (timerSeconds % 5 == 0) {
                session.broadcastTitle("", "&a&l" + timerSeconds + " &7seconds till the game starts!", 10, 0, 3, true);
            }

            timerSeconds--;
        }

        if (session.isStarted()) {
            int secondsLeft = session.getMaxTime() - ((int)(System.currentTimeMillis() / 1000) - (int)(startTime / 1000));
            if (secondsLeft <= 0) {
                //TODO: Force end the game
            } else if (secondsLeft == 10) {
                session.broadcastTitle("", "&4&l" + secondsLeft + " &cseconds till the game ends!", 15, 0, 3, true);
            } else if (secondsLeft == 30) {
                session.broadcastTitle("", "&4&l" + secondsLeft + " &cseconds till the game ends!", 15, 0, 3, true);
            } else if (secondsLeft == 60) {
                session.broadcastTitle("", "&4&l1 minute till the game ends!", 15, 0, 3, true);
            }
        }
    }
}
