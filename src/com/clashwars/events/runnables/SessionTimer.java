package com.clashwars.events.runnables;

import com.clashwars.events.Events;
import com.clashwars.events.StartMessages;
import com.clashwars.events.events.GameSession;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Each session has a timer that starts when enough players have joined the session.
 * After the session is started the timer will continue to run every second for the game ending time.
 */
public class SessionTimer extends BukkitRunnable {

    private int sessionID;
    private Long startTime;

    private String[] messages = StartMessages.getMessages();
    private boolean timerRunning = false;
    private int timerSeconds = -1;

    private boolean resumeTimerRunning = false;
    private int resumeTimerSeconds = -1;

    public SessionTimer(int sessionID) {
        this.sessionID = sessionID;
        runTaskTimer(Events.inst(), 0, 20);
    }


    /** Start the countdown timer if there is no timer running */
    public void startCountdownTimer(int seconds) {
        if (!isResumeTimerRunning() && !isCountdownTimerRunning()) {
            timerSeconds = seconds;
            timerRunning = true;
        }
    }

    /** Stop the countdown timer if it's running */
    public void stopCountdownTimer() {
        if (isCountdownTimerRunning()) {
            timerRunning = false;
            timerSeconds = -1;
        }
    }

    /** Sets the time left on the countdown timer. Only if it's lower than the current time left. */
    public void setCountdownTimeRemaining(int seconds) {
        timerSeconds = Math.min(timerSeconds, seconds);
    }

    /** Returns true if the countdown timer has started */
    public boolean isCountdownTimerRunning() {
        return timerRunning;
    }

    /** Get the amount of seconds left on the countdown timer */
    public int getCountdownTime() {
        return timerSeconds;
    }


    /** Start the resume timer if there is no timer running */
    public void startResumeTimer(int seconds) {
        if (!isCountdownTimerRunning() && !isResumeTimerRunning()) {
            resumeTimerSeconds = seconds;
            resumeTimerRunning = true;
        }
    }

    /** Stop the resume timer if there it's running */
    public void stopResumeTimer() {
        if (isResumeTimerRunning()) {
            resumeTimerRunning = false;
            resumeTimerSeconds = -1;
        }
    }

    /** Sets the time left on the resume timer. Only if it's lower than the current time left. */
    public void setResumeTimeRemaining(int seconds) {
        resumeTimerSeconds = Math.min(resumeTimerSeconds, seconds);
    }

    /** Returns true if the resume timer has started */
    public boolean isResumeTimerRunning() {
        return resumeTimerRunning;
    }

    /** Get the amount of seconds left on the resume timer */
    public int getResumeTime() {
        return resumeTimerSeconds;
    }



    @Override
    public void run() {
        GameSession session = Events.inst().sm.getSession(sessionID);
        if (session == null) {
            cancel();
            return;
        }

        if (timerRunning) {
            if (timerSeconds <= 10 && timerSeconds > 3) {
                session.playSound(Sound.NOTE_STICKS, 0.5f, 1, true);
            }

            if (timerSeconds == 10) {
                session.broadcastTitle("", "&a&l" + timerSeconds + " &7seconds till the game starts!", 30, 5, 5, true);
                session.startCountdown();
            } else if (timerSeconds == 5) {
                session.broadcastTitle("", "&a&l" + timerSeconds + " &7seconds till the game starts!", 30, 0, 5, true);
            } else if (timerSeconds <= 3 && timerSeconds > 0) {
                if (timerSeconds == 3) {
                    session.lock();
                }
                session.broadcastTitle("&a&l" + timerSeconds, "&7" + messages[timerSeconds - 1], 18, 0, 1, true);
                session.playSound(Sound.NOTE_PLING, 0.8f, 2, true);
            } else if (timerSeconds == 0) {
                startTime = System.currentTimeMillis();
                session.playSound(Sound.WITHER_SPAWN, 0.8f, 2, true);
                session.start();
            }

            timerSeconds--;
        }


        if (resumeTimerRunning) {
            if (resumeTimerSeconds <= 10 && resumeTimerSeconds > 3) {
                session.playSound(Sound.NOTE_STICKS, 0.5f, 1, true);
            }

            if (resumeTimerSeconds == 10) {
                session.broadcastTitle("", "&a&l" + resumeTimerSeconds + " &7seconds till the game continues!", 30, 5, 5, true);
            } else if (resumeTimerSeconds == 5) {
                session.broadcastTitle("", "&a&l" + resumeTimerSeconds + " &7seconds till the game continues!", 30, 0, 5, true);
            } else if (resumeTimerSeconds <= 3 && resumeTimerSeconds > 0) {
                session.broadcastTitle("&a&l" + resumeTimerSeconds, "&7" + messages[resumeTimerSeconds - 1], 18, 0, 1, true);
                session.playSound(Sound.NOTE_PLING, 0.8f, 2, true);
            } else if (resumeTimerSeconds == 0) {
                session.playSound(Sound.WITHER_SPAWN, 0.8f, 2, true);
                session.resume();
            }

            resumeTimerSeconds--;
        }

        if (session.isStarted()) {
            if (startTime == null || startTime <= 0l) {
                startTime = session.getData().getStartTime();
            }
            int secondsLeft = session.getMaxTime() - ((int)(System.currentTimeMillis() / 1000) - (int)(startTime / 1000));
            if (secondsLeft <= 0) {
                session.forceEnd();
            } else if (secondsLeft == 10) {
                session.broadcastTitle("", "&4&l" + secondsLeft + " &cseconds till the game ends!", 30, 0, 5, true);
            } else if (secondsLeft == 30) {
                session.broadcastTitle("", "&4&l" + secondsLeft + " &cseconds till the game ends!", 30, 0, 5, true);
            } else if (secondsLeft == 60) {
                session.broadcastTitle("", "&4&l1 minute till the game ends!", 30, 0, 5, true);
            }
        }
    }
}
