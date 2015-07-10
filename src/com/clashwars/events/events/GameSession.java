package com.clashwars.events.events;

import com.clashwars.cwcore.packet.Title;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.Events;
import com.clashwars.events.maps.EventMap;
import com.clashwars.events.player.CWPlayer;
import com.clashwars.events.runnables.SessionTimer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GameSession {

    protected GameSession session;
    protected Events events;

    protected SessionTimer timer;

    protected int ID;
    protected EventType type;
    protected String mapName;

    protected State state = State.CLOSED;
    protected List<Player> players = new ArrayList<Player>();
    protected List<Player> vipPlayers = new ArrayList<Player>();
    protected List<Player> spectators = new ArrayList<Player>();

    private EventMap map;

    protected int maxTime = 300; /** Time in seconds should be overwritten to modify */


    /**
     * Creates a new GameSession with the given ID, EventType and map name.
     * Sessions should only be created through the SessionManager.
     */
    public GameSession(int ID, EventType type, String mapName) {
        events = Events.inst();
        session = this;
        this.ID = ID;
        this.type = type;
        this.mapName = mapName;

        timer = new SessionTimer(session);

        map = events.mm.getMap(type, mapName);
        if (!map.isClosed()) {
            state = State.OPENED;
        }
    }


    /**
     * Checks if the given player can join this session or not.
     * If the player has 'events.joinfull' he can always join even if it's full.
     * If the player has 'events.vip' he can join if the game is full and there are VIP spots left.
     */
    public JoinType canJoin(Player player) {
        if (map == null || !map.isValid()) {
            return JoinType.INVALID;
        }
        if (map.isClosed()) {
            return JoinType.CLOSED;
        }
        if (isResetting()) {
            return JoinType.RESETTING;
        }
        if (isEnded()) {
            return JoinType.ENDED;
        }

        if (isStarted()) {
            return JoinType.SPECTATE;
        }

        //Check if there is space to join.
        if (!player.hasPermission("events.joinfull")) {
            if (players.size() >= map.getMaxPlayers()) {
                if (!player.hasPermission("events.vip")) {
                    return JoinType.FULL;
                }
                if (vipPlayers.size() >= map.getVipSpots()) {
                    return JoinType.FULL;
                }
            }
        }

        return JoinType.JOIN;
    }

    /**
     * Add the given player to this session.
     * Make sure to check canJoin() before joining (this has 0 checks if the player can join or not)
     */
    public void join(Player player) {
        CWPlayer cwp = events.pm.getPlayer(player);
        cwp.setSession(session);

        if (isStarted()) {
            spectators.add(player);
            broadcast("&6&l+&7" + player.getDisplayName() + " &8(&3&lS&8)", true);
        } else if (player.hasPermission("events.vip") && vipPlayers.size() < map.getVipSpots()) {
            vipPlayers.add(player);
            broadcast("&6&l+&3" + player.getDisplayName(), true);
        } else {
            players.add(player);
            broadcast("&6&l+&3" + player.getDisplayName(), true);
        }

        if (isOpened()) {
            if (getPlayerCount(false) >= map.getMaxPlayers()) {
                //If game is filled up (max players) set the timer to 10.
                timer.setTimeRemaining(10);
            } else if (getPlayerCount(false) >= map.getMinPlayers()) {
                //If game has enough players start the countdown timer.
                timer.startTimer();
            }
        }
    }

    /** Remove the given player from this session. */
    public void leave(Player player) {
        CWPlayer cwp = events.pm.getPlayer(player);
        cwp.removeSession();

        if (players.contains(player)) {
            players.remove(player);
            broadcast("&4&l-&7" + player.getDisplayName(), true);
        } else if (vipPlayers.contains(player)) {
            vipPlayers.remove(player);
            broadcast("&4&l-&7" + player.getDisplayName(), true);
        } else if (spectators.contains(player)) {
            spectators.remove(player);
            broadcast("&4&l-&7" + player.getDisplayName()  + " &8(&cS&8)", true);
        }

        if (isOpened()) {
            if (getPlayerCount(false) <= 0) {
                //If no players left remove the session.
                //TODO: Remove session
            } if (getPlayerCount(false) < map.getMinPlayers()) {
                //If not enough players anymore stop the timer.
                timer.stopTimer();
                broadcast("&c&lThere aren't enough players to start the game anymore!", true);
                broadcast("&cPlease wait for more players to join again.", true);
            }
        }
    }


    /** Broadcast a message to all players in the session */
    public void broadcast(String message, boolean spectators) {
        List<Player> allPlayers = getAllPlayers(spectators);
        for (Player p : allPlayers) {
            p.sendMessage(CWUtil.integrateColor(message));
        }
    }

    /** Broadcast a message on the action bar to all players in the session */
    public void broadcastBar(String prefix, String message, boolean spectators) {
        List<Player> allPlayers = getAllPlayers(spectators);
        for (Player p : allPlayers) {
            CWUtil.sendActionBar(p, prefix, message);
        }
    }

    /** Broadcast a title/subtitle to all players in the session */
    public void broadcastTitle(String title, String subtitle, int ticks, int fadeInTicks, int fadeOutTicks, boolean spectators) {
        Title t = new Title(title, subtitle, fadeInTicks, ticks, fadeOutTicks);
        t.setTimingsToTicks();
        List<Player> allPlayers = getAllPlayers(spectators);
        for (Player p : allPlayers) {
            t.send(p);
        }
    }

    /** Play a sound for all players in the session */
    public void playSound(Sound sound, float volume, float pitch, boolean spectators) {
        List<Player> allPlayers = getAllPlayers(spectators);
        for (Player p : allPlayers) {
            p.playSound(p.getLocation(), sound, volume, pitch);
        }
    }



    /** Get a list of all players in the session */
    public List<Player> getAllPlayers(boolean spectators) {
        List<Player> playerList = new ArrayList<Player>(players);
        playerList.addAll(vipPlayers);
        if (spectators) {
            playerList.addAll(this.spectators);
        }
        return playerList;
    }

    /** Get the total amount of players in this session. */
    public int getPlayerCount(boolean spectators) {
        return players.size() + vipPlayers.size() + (spectators ? this.spectators.size() : 0);
    }


    /** Returns true if the session is opened */
    public boolean isOpened() {
        return state == State.OPENED;
    }

    /** Returns true if the session is started */
    public boolean isStarted() {
        return state == State.STARTED;
    }

    /** Returns true if the session is ened */
    public boolean isEnded() {
        return state == State.ENDED;
    }

    /** Returns true if the session is resetting */
    public boolean isResetting() {
        return state == State.RESETTING;
    }


    /** Get the GameSession class */
    public GameSession getSession() {
        return session;
    }

    /** Get the unique session ID */
    public int getID() {
        return ID;
    }

    /** Get the EventType of this session */
    public EventType getType() {
        return type;
    }

    /** Get the name of the map this session belongs to */
    public String getMapName() {
        return mapName;
    }

    /** Get the map this session belongs to */
    public EventMap getMap() {
        return map;
    }

    /** Get the time in seconds the session can run for max */
    public int getMaxTime() {
        return maxTime;
    }

}
