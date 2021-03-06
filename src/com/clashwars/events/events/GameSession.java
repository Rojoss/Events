package com.clashwars.events.events;

import com.clashwars.cwcore.packet.Title;
import com.clashwars.cwcore.player.Vanish;
import com.clashwars.cwcore.scoreboard.CWBoard;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.Events;
import com.clashwars.events.maps.EventMap;
import com.clashwars.events.modifiers.Modifier;
import com.clashwars.events.modifiers.ModifierOption;
import com.clashwars.events.player.CWPlayer;
import com.clashwars.events.runnables.SessionTimer;
import com.clashwars.events.util.Equipment;
import com.clashwars.events.util.Util;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.NameTagVisibility;

import java.util.*;

public class GameSession {

    protected GameSession session;
    protected Events events;

    protected SessionData data;
    protected SessionTimer timer;

    private BaseEvent event;
    private EventMap map;

    protected CWBoard board;

    List<Location> spawnLocs = new ArrayList<Location>();
    protected int maxTime = 300; /** Time in seconds should be overwritten to modify */


    /**
     * Creates a new GameSession with the given ID, EventType and map name.
     * Sessions should only be created through the SessionManager.
     */
    public GameSession(SessionData data, boolean loadedFromConfig) {
        events = Events.inst();
        session = this;
        this.data = data;

        map = events.mm.getMap(getType(), getMapName());
        if (map != null) {
            map.validateMap();
        }
        event = getType().getEventClass();

        if (event == null || map == null || !map.isValid() || map.isClosed()) {
            setState(State.CLOSED);
            new BukkitRunnable() {
                @Override
                public void run() {
                    delete();
                }
            }.runTaskLater(events, 5);
            return;
        } else {
            timer = new SessionTimer(getID());
            spawnLocs = new ArrayList<Location>(getMap().getMultiLocs("spawn").values());

            if (loadedFromConfig) {
                //Only put events on hold that have been started already.
                if (!isStarted()) {
                    broadcast("&4&lThe session has been removed!", true);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            delete();
                        }
                    }.runTaskLater(events, 5);
                    return;
                }
                onHold();
            } else {
                setState(State.OPENED);
            }
        }

        board = CWBoard.get(map.getTag());
        board.register();
        board.addTeam("spectators","&d","","&dSpectators", false, true, NameTagVisibility.ALWAYS);

        Util.updateStatus(map, session);
        save();
    }

    public void unload() {
        //-
    }


    //==========================================
    //======== Session Joining/Leaving =========
    //==========================================

    /**
     * Checks if the given player can join this session or not.
     * If the player has 'events.joinfull' he can always join even if it's full.
     * If the player has 'events.vip' he can join if the game is full and there are VIP spots left.
     */
    public JoinType canJoin(Player player, boolean spectateIfStarted) {
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

        CWPlayer cwp = events.pm.getPlayer(player);
        if (isStarted()) {
            if (cwp.inSession()) {
                return JoinType.IN_GAME;
            }
            if (spectateIfStarted) {
                return JoinType.SPECTATE;
            } else {
                return JoinType.STARTED;
            }
        }

        if (isOnHold()) {
            if (hasPlayer(player.getUniqueId(), true, true, true)) {
                return JoinType.JOIN_BACK;
            } else {
                if (cwp.inSession()) {
                    return JoinType.IN_GAME;
                }
                if (spectateIfStarted) {
                    return JoinType.SPECTATE;
                } else {
                    return JoinType.STARTED;
                }
            }
        }

        if (cwp.inSession()) {
            return JoinType.IN_GAME;
        }

        //Check if there is space to join.
        if (getPlayerSize() >= map.getMaxPlayers()) {
            if (!player.hasPermission("events.vip")) {
                if (spectateIfStarted) {
                    return JoinType.SPECTATE;
                } else {
                    return JoinType.FULL;
                }
            }
            if (getVipPlayerSize() >= map.getVipSpots()) {
                if (spectateIfStarted) {
                    return JoinType.SPECTATE;
                } else {
                    return JoinType.FULL;
                }
            }
        }

        if (isCountdown()) {
            if (timer.getCountdownTime() <= 3) {
                if (spectateIfStarted) {
                    return JoinType.SPECTATE;
                } else {
                    return JoinType.STARTED;
                }
            }
            return JoinType.JOIN;
        } else {
            return JoinType.QUEUE;
        }
    }

    /**
     * Add the given player to this session.
     * Make sure to check canJoin() before joining (this has 0 checks if the player can join or not)
     */
    public void join(Player player) {
        UUID uuid = player.getUniqueId();
        CWPlayer cwp = events.pm.getPlayer(player);
        cwp.setSession(session);
        cwp.setSignJoinLoc(player.getLocation());

        if (isOnHold()) {
            //Joining back while the session is on hold. (Only allow previous players to join. Others will be put in spectate mode)
            if (hasPlayer(uuid, true, true, false)) {
                cwp.setSpectating(false);
                if (hasVip(uuid)) {
                    broadcast("&6&l+&3" + player.getDisplayName(), true);
                } else {
                    broadcast("&6&l+&3" + player.getDisplayName(), true);
                }
                if (session.isCountdown()) {
                    teleportPlayer(player);
                }
            } else {
                addSpectator(uuid);
                cwp.setSpectating(true);
                broadcast("&6&l+&3" + player.getDisplayName() + " &8(&d&lS&8)", true);
                if (session.isCountdown() || session.isStarted()) {
                    teleportPlayer(player);
                }
            }

            //If all players (expect spectators) join back start the game again.
            if (timer.getResumeTime() > 5 && getAllPlayers(false).size() == getAllOnlinePlayers(false).size()) {
                timer.setResumeTimeRemaining(4);
            }
        } else {
            if (hasPlayer(uuid, true, true, true)) {
                return;
            }
            if (isStarted()) {
                cwp.setSpectating(true);
                addSpectator(uuid);
                broadcast("&6&l+&3" + player.getDisplayName() + " &8(&d&lS&8)", true);
            } else if (player.hasPermission("events.vip") && getVipPlayerSize() < map.getVipSpots()) {
                cwp.setSpectating(false);
                addVip(uuid);
                broadcast("&6&l+&3" + player.getDisplayName(), true);
            } else {
                cwp.setSpectating(false);
                addPlayer(uuid);
                broadcast("&6&l+&3" + player.getDisplayName(), true);
            }

            if (session.isCountdown() || session.isStarted()) {
                teleportPlayer(player);
                sendModifiersMessage(player, null);
            }

            //Start the countdown if enough players have joined.
            if (isJoinable()) {
                if (!isCountdown() && getPlayerCount(false) >= map.getMaxPlayers()) {
                   //Force start the 10 second countdown if max players joined.
                    if (!timer.isCountdownTimerRunning()) {
                        timer.startCountdownTimer(10);
                    } else {
                        timer.setCountdownTimeRemaining(10);
                    }
                } else if (!timer.isCountdownTimerRunning() && getPlayerCount(false) >= map.getMinPlayers()) {
                    //If the minimum amount of players have joined start/resume the 30 second countdown.
                    if (isCountdown()) {
                        timer.startCountdownTimer(10);
                    } else {
                        timer.startCountdownTimer(30);
                    }
                }
            }
        }

        if (board != null) {
            board.addPlayer(player);
        }
        Util.updateStatus(map, session);
        save();
    }

    /** Remove the given player from this session. */
    public void leave(OfflinePlayer player, boolean force) {
        UUID uuid = player.getUniqueId();
        CWPlayer cwp = events.pm.getPlayer(player);

        String playerName = player.getName();
        if (player.isOnline()) {
            playerName = ((Player)player).getDisplayName();
            Util.teleportLobby(((Player)player));
        }

        if (hasPlayer(uuid)) {
            removePlayer(uuid);
            if (!force)
                broadcast("&4&l-&7" + playerName, true);
        } else if (hasVip(uuid)) {
            removeVip(uuid);
            if (!force)
                broadcast("&4&l-&7" + playerName, true);
        } else if (hasSpectator(uuid)) {
            removeSpectator(uuid);
            if (!force)
                broadcast("&4&l-&7" + playerName  + " &8(&dS&8)", true);
        }

        if (board != null) {
            board.removePlayer(uuid);
            board.resetScore(player.getName());
        }
        Util.updateStatus(map, session);
        save();

        if (force) {
            return;
        }
        if (getPlayerCount(false) <= 0) {
            //If no players left reset the session.
            reset();
        } else if (getPlayerCount(false) < 2) {
            //If not enough players anymore stop the timer or end the game.
            if (isStarted()) {
                if (data.hasPotentialWinners()) {
                    end(data.getPotentialWinners());
                } else {
                    broadcast("&c&lThere aren't enough players remaining to continue the game!", true);
                    forceEnd();
                }
            } else if (isJoinable()) {
                stopCountdown();
            }
        }
    }


    /** Set player to spectator mode if he's playing */
    public void switchToSpectator(Player player) {
        UUID uuid = player.getUniqueId();
        CWPlayer cwp = events.pm.getPlayer(player);
        if (!hasPlayer(uuid, true, true, false) || cwp.isSpectating() || !cwp.inSession()) {
            return;
        }

        if (hasPlayer(uuid)) {
            removePlayer(uuid);
        }
        if (hasVip(uuid)) {
            removeVip(uuid);
        }

        if (!hasSpectator(uuid)) {
            addSpectator(uuid);
        }
        cwp.setSpectating(true);

        CWUtil.resetPlayer(player, GameMode.SURVIVAL);
        player.setAllowFlight(true);
        player.setFlying(true);
        Equipment.SPECTATOR.equip(player);

        player.teleport(spawnLocs.get(0));
        Util.updateStatus(map, session);

        if (getPlayerCount(false) <= 0) {
            //If no players left reset the session.
            reset();
        } else if (getPlayerCount(false) < 2) {
            //If not enough players anymore stop the timer or end the game.
            if (isStarted()) {
                if (data.hasPotentialWinners()) {
                    end(data.getPotentialWinners());
                } else {
                    broadcast("&c&lThere aren't enough players remaining to continue the game!", true);
                    forceEnd();
                }
            } else if (isJoinable()) {
                stopCountdown();
            }
        }
    }


    //==========================================
    //======== Session State Management ========
    //==========================================

    /** Open the session for players to join. */
    public void open() {
        setState(State.OPENED);
    }

    /** Start the 10 second countdown and teleport all players to the map. */
    public void startCountdown() {
        setState(State.COUNTDOWN);
        calculateModifiers();
        for (Player player : getAllOnlinePlayers(true)) {
            teleportPlayer(player);
        }
        if (board != null) {
            board.show(true);
        }
    }

    /** Stop the countdown timer */
    public void stopCountdown() {
        timer.stopCountdownTimer();
        //Do it silent during the 30 to 10 second timer as lots of people will be joining/leaving.
        if (isCountdown()) {
            broadcast("&c&lThere aren't enough players to start the game anymore!", true);
            broadcast("&cPlease wait for more players to join again.", true);
        }
    }

    /** Lock the game at the 3 second timer so players can't join anymore. For team creation and such. */
    public void lock() {
        //--
    }

    /** Start the game. */
    public void start() {
        data.setStartTime(System.currentTimeMillis());
        data.setFinalPlayers(getAllPlayers(false));
        setState(State.STARTED);
        broadcast("&6&lThe game has &a&lstarted&6&l!", true);
    }

    /** End the game with the specified winner(s). (can be null for no winners) */
    public void end(List<UUID> winners) {
        if (winners != null) {
            if (winners.size() > 1) {
                List<String> winnerNames = new ArrayList<String>();
                for (UUID winner : winners) {
                    winnerNames.add(CWUtil.getName(winner));
                }
                broadcast("&a&l" + CWUtil.implode(winnerNames, "&8&l, &a&l", " &8&l& &a&l") + " &6&lwin!", true);
            } else if (winners.size() > 0) {
                broadcast("&a&l" + CWUtil.getName(winners.get(0)) + " &6&lwins!", true);
            }
        }

        setState(State.ENDED);
        new BukkitRunnable() {
            @Override
            public void run() {
                reset();
            }
        }.runTaskLater(events, 100);
    }

    public void end(UUID winner) {
        end(Arrays.asList(new UUID[] {winner}));
    }

    /** Force end the game without any winners and no stats saved. */
    public void forceEnd() {
        end((List<UUID>)null);
    }

    /** Reset the game/map. */
    public boolean reset() {
        if (isResetting()) {
            return false;
        }
        setState(State.RESETTING);
        broadcast("&6&lThe map is resetting!", true);

        for (Player player : getAllOnlinePlayers(true)) {
            leave(player, true);
        }
        return true;
    }

    /** Delete the session so a new session can be opened for this map */
    public void delete() {
        if (session != null) {
            session = null;
            timer.cancel();
            for (Player player : getAllOnlinePlayers(true)) {
                leave(player, true);
            }
            if (board != null) {
                board.hide(null);
                board.unregister();
                board.delete();
            }
            Util.updateStatus(map, null);
            events.sm.deleteSession(getID());
        }
    }

    /** Resume the session after it's put on hold. */
    public void resume() {
        if (getPlayerCount(false) < map.getMinPlayers()) {
            broadcast("&c&lNot enough players joined back!", true);
            reset();
            return;
        }
        setState(State.STARTED);
        broadcast("&6The game has been &a&lresumed&6!", true);
    }

    /** Put the session on hold waiting for new players to join. */
    public void onHold() {
        setState(State.ON_HOLD);

        timer.startResumeTimer(20);
        broadcast("&6&lWaiting for players to join back...", true);

        //Add back all players that are online.
        List<Player> onlinePlayers = getAllOnlinePlayers(true);
        for (Player player : onlinePlayers) {
            join(player);
        }
    }



    //==========================================
    //============== Session Utils =============
    //==========================================

    public void teleportPlayer(Player player) {
        CWPlayer cwp = events.pm.getPlayer(player);
        CWUtil.resetPlayer(player, GameMode.SURVIVAL);
        player.teleport(getTeleportLocation(cwp));
        if (isOpened() || isResetting()) {
            Equipment.LOBBY.equip(player);
            return;
        }
        if (cwp.isSpectating()) {
            player.setAllowFlight(true);
            player.setFlying(true);
            Equipment.SPECTATOR.equip(player);
        } else {
            Util.equipItems(player, event.getEquipment(this));
        }
    }

    public Location getTeleportLocation(CWPlayer cwp) {
        Location loc = null;
        if (isOpened() || isResetting()) {
            if (cwp.getSignJoinLoc() != null) {
                return cwp.getSignJoinLoc();
            }
            return cwp.getWorld().getSpawnLocation();
        }
        if (cwp.isSpectating() || spawnLocs.size() <= 0) {
            loc = getMap().getCuboid("map").getCenterLoc(); //TODO: Teleport to safe location
            cwp.setTeleportID(-1);
        } else {
            if (cwp.getTeleportID() >= 0 && spawnLocs.size() > cwp.getTeleportID()) {
                loc = spawnLocs.get(cwp.getTeleportID());
            } else {
                loc = spawnLocs.get(data.getTeleportID());
                cwp.setTeleportID(data.getTeleportID());
                data.setTeleportID(data.getTeleportID() + 1);
                if (data.getTeleportID() >= spawnLocs.size()) {
                    data.setTeleportID(0);
                }
            }
        }
        return loc;
    }

    public void setupTeams(int playersPerTeam) {
        List<UUID> allPlayers = getAllPlayers(false);

        int count = 0;
        int id = 0;
        String team = "";

        for (UUID uuid : allPlayers) {
            if (team.isEmpty() || count % playersPerTeam == 0) {
                String prefix = CWUtil.getPrefix(true, id);
                board.addTeam("team-" + id, prefix, "", prefix + CWUtil.getTeamName(prefix), false, false);
                team = "team-" + id;
                id++;
            }
            board.joinTeam(team, events.getServer().getOfflinePlayer(uuid));
            count++;
        }
    }

    /** Broadcast a message to all players in the session */
    public void broadcast(String message, boolean spectators) {
        List<Player> allPlayers = getAllOnlinePlayers(spectators);
        for (Player player : allPlayers) {
            player.sendMessage(CWUtil.integrateColor(message));
        }
    }

    /** Broadcast a message on the action bar to all players in the session */
    public void broadcastBar(String prefix, String message, boolean spectators) {
        List<Player> allPlayers = getAllOnlinePlayers(spectators);
        for (Player player : allPlayers) {
            CWUtil.sendActionBar(player, prefix, message);
        }
    }

    /** Broadcast a title/subtitle to all players in the session */
    public void broadcastTitle(String title, String subtitle, int ticks, int fadeInTicks, int fadeOutTicks, boolean spectators) {
        Title t = new Title(title, subtitle, fadeInTicks, ticks, fadeOutTicks);
        t.setTimingsToTicks();
        List<Player> allPlayers = getAllOnlinePlayers(spectators);
        for (Player player : allPlayers) {
            t.send(player);
        }
    }

    /** Play a sound for all players in the session */
    public void playSound(Sound sound, float volume, float pitch, boolean spectators) {
        List<Player> allPlayers = getAllOnlinePlayers(spectators);
        for (Player player : allPlayers) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }



    //==========================================
    //============== Player Utils ==============
    //==========================================

    /** Get a player by UUID. Returns null if the player is invalid/offline */
    public Player getPlayer(UUID uuid) {
        return events.getServer().getPlayer(uuid);
    }

    /** Get a offline player by UUID. Returns null if the player is invalid */
    public OfflinePlayer getOfflinePlayer(UUID uuid) {
        return events.getServer().getOfflinePlayer(uuid);
    }



    //==========================================
    //============ Player management ===========
    //==========================================

    /** Get a list of all players in the session */
    public List<UUID> getAllPlayers(boolean spectators) {
        List<UUID> playerList = new ArrayList<UUID>(getPlayers());
        playerList.addAll(getVips());
        if (spectators) {
            playerList.addAll(getSpectators());
        }
        return playerList;
    }

    /** Get a list of all players in this session that are online */
    public List<Player> getAllOnlinePlayers(boolean spectators) {
        List<Player> playerList = new ArrayList<Player>();
        List<UUID> allPlayers = getAllPlayers(spectators);
        for (UUID uuid : allPlayers) {
            Player player = getPlayer(uuid);
            if (player != null) {
                playerList.add(player);
            }
        }
        return playerList;
    }

    /** Get the total amount of players in this session. */
    public int getPlayerCount(boolean spectators) {
        return data.getPlayers().size() + data.getVipPlayers().size() + (spectators ? this.data.getSpectators().size() : 0);
    }

    /** Checks if the session has the given player in any of the specified groups. */
    public boolean hasPlayer(UUID player, boolean regular, boolean vip, boolean spectator) {
        if (regular && hasPlayer(player)) {
            return true;
        }
        if (vip && hasVip(player)) {
            return true;
        }
        if (spectator && hasSpectator(player)) {
            return true;
        }
        return false;
    }

    /** Get all regular players in this session */
    public List<UUID> getPlayers() {
        return data.getPlayers();
    }
    /** Get the amount of regular players in this session */
    public int getPlayerSize() {
        return data.getPlayers().size();
    }
    /** Add a regular player to this session */
    public void addPlayer(UUID player) {
        data.addPlayer(player);
        save();
    }
    /** Remove a regular player from this session */
    public void removePlayer(UUID player) {
        data.removePlayer(player);
        save();
    }
    /** Check if the session has this regular player */
    public boolean hasPlayer(UUID player) {
        return data.getPlayers().contains(player);
    }

    /** Get all VIP players in this session */
    public List<UUID> getVips() {
        return data.getVipPlayers();
    }
    /** Get the amount of VIP players in this session */
    public int getVipPlayerSize() {
        return data.getVipPlayers().size();
    }
    /** Add a VIP player to this session */
    public void addVip(UUID player) {
        data.addVip(player);
        save();
    }
    /** Remove a VIP player from this session */
    public void removeVip(UUID player) {
        data.removeVip(player);
        save();
    }
    /** Check if the session has this VIP player */
    public boolean hasVip(UUID player) {
        return data.getVipPlayers().contains(player);
    }

    /** Get all Spectator players in this session */
    public List<UUID> getSpectators() {
        return data.getSpectators();
    }
    /** Get the amount of Spectator players in this session */
    public int getSpecPlayerSize() {
        return data.getSpectators().size();
    }
    /** Add a Spectator player to this session */
    public void addSpectator(UUID player) {
        data.addSpectator(player);
        if (board != null) {
            board.joinTeam("spectators", events.getServer().getOfflinePlayer(player));
        }
        Vanish.vanish(player);
        save();
    }
    /** Remove a Spectator player from this session */
    public void removeSpectator(UUID player) {
        data.removeSpectator(player);
        if (board != null) {
            board.leaveTeam("spectators", events.getServer().getOfflinePlayer(player));
        }
        Vanish.unvanish(player);
        save();
    }
    /** Check if the session has this Spectator player */
    public boolean hasSpectator(UUID player) {
        return data.getSpectators().contains(player);
    }

    /** Get all players that were in the session when the game started. */
    public List<UUID> getFinalPlayers() {
        return data.getFinalPlayers();
    }
    /** Get the amount of players that were in the session when the game started */
    public int getFinalPlayerSize() {
        return data.getFinalPlayers().size();
    }

    /** Get all potential winners in this session */
    public List<UUID> getPotentialWinners() {
        return data.getPotentialWinners();
    }
    /** Set all the potential winners in this session. If it ends these players will be made winner. */
    public void setPotentialWinners(List<UUID> potentialWinners) {
        data.setPotentialWinners(potentialWinners);
        save();
    }
    /** Get the amount of potential winners */
    public int getPotentialWinnersSize() {
        return data.getPotentialWinners().size();
    }
    /** Add a regular player to this session */
    public void addPotentialWinner(UUID player) {
        data.addPotentialWinner(player);
        save();
    }
    /** Remove a potential winner from the list. */
    public void removePotentialWinner(UUID player) {
        data.removePotentialWinner(player);
        save();
    }
    /** Check if the session has this player has potential winner. */
    public boolean hasPotentialWinner(UUID player) {
        return data.getPotentialWinners().contains(player);
    }


    //==========================================
    //=============== Game state ===============
    //==========================================

    /** Get the current game state of this session */
    public State getState() {
        return data.getState();
    }

    /** Set the game state of this session */
    public void setState(State state) {
        data.setState(state);
        Util.updateStatus(map, session);
        save();
    }

    /** Returns true if the session is opened */
    public boolean isOpened() {
        return getState() == State.OPENED;
    }

    /** Returns true if the sessions 10s countdown has start */
    public boolean isCountdown() {
        return getState() == State.COUNTDOWN;
    }

    /** Returns true if the session is opened */
    public boolean isJoinable() {
        return getState() == State.OPENED || getState() == State.COUNTDOWN;
    }

    /** Returns true if the session is started */
    public boolean isStarted() {
        return getState() == State.STARTED;
    }

    /** Returns true if the session is on hold */
    public boolean isOnHold() {
        return getState() == State.ON_HOLD;
    }

    /** Returns true if the session is ened */
    public boolean isEnded() {
        return getState() == State.ENDED;
    }

    /** Returns true if the session is resetting */
    public boolean isResetting() {
        return getState() == State.RESETTING;
    }



    //==========================================
    //================ Modifiers ===============
    //==========================================

    /** Returns true if the modifier options have been set and false if not */
    public boolean hasModifierOptions() {
        return getModifierOptions().size() > 0;
    }

    /** Get a hasmap with all the modifiers and options */
    public HashMap<Modifier, ModifierOption> getModifierOptions() {
        return data.getModifierValues();
    }

    /** Get the modifier option for the specified modifier. Returns null if there is no option defined for the modifier. */
    public ModifierOption getModifierOption(Modifier modifier) {
        HashMap<Modifier, ModifierOption> options = data.getModifierValues();
        if (options.isEmpty() || !options.containsKey(modifier)) {
            return null;
        }
        return options.get(modifier);
    }

    /**
     * Calculate all the modifiers.
     * It has a 25% chance to be random based on the modifier value weights.
     * If that doesn't happen it will set the value based on the players in the session their preferences.
     */
    private void calculateModifiers() {
        if (hasModifierOptions()) {
            return;
        }
        HashMap<Modifier, ModifierOption> resultMap = new HashMap<Modifier, ModifierOption>();
        for (Modifier modifier : event.getModifiers()) {
            ModifierOption[] options = modifier.getOptions();

            float totalWeight = 0.0f;
            for (ModifierOption mo : options) {
                totalWeight += mo.weight;
            }

            //25% chance to ignore player preferences and randomized only based on modifier weights.
            if (CWUtil.randomFloat() <= 1f) {
                double random = Math.random() * totalWeight;
                for (ModifierOption mo : options) {
                    random -= mo.weight;
                    if (random <= 0.0d) {
                        resultMap.put(modifier, mo);
                        break;
                    }
                }
                continue;
            }

            //Get the favourite preference of all the players.
            //TODO: Do this...
        }
        data.setModifierValues(resultMap);
        save();

        List<Player> onlinePlayers = getAllOnlinePlayers(true);
        for (Player player : onlinePlayers) {
            sendModifiersMessage(player, resultMap);
        }
    }

    /**
     * Send a message with all modifier values to the specified player.
     * The modifierOptions hashmap can be null but when sending to all players it should be provided.
     * It won't send a message if the modifiers haven't been calculated.
     */
    public void sendModifiersMessage(Player player, HashMap<Modifier, ModifierOption> modifierOptions) {
        if (modifierOptions == null) {
            modifierOptions = getModifierOptions();
        }
        if (modifierOptions.size() <= 0) {
            return;
        }
        player.sendMessage(CWUtil.integrateColor("&7&l----------- &4&lGAME MODIFIERS &7&l-----------"));
        for (Map.Entry<Modifier, ModifierOption> entry : modifierOptions.entrySet()) {
            player.sendMessage(CWUtil.integrateColor("&6" + CWUtil.capitalize(entry.getKey().toString().toLowerCase().split("_")[1]) + "&8: &7" + entry.getValue().name));
        }
    }


    //==========================================
    //============== Session data ==============
    //==========================================

    /** Get the GameSession class */
    public GameSession getSession() {
        return session;
    }

    /** Get the SessionData from this session */
    public SessionData getData() {
        return data;
    }

    /** Set the SessionData for this session */
    public void setData(SessionData data) {
        this.data = data;
        save();
    }

    /** Get the unique session ID */
    public int getID() {
        return data.getSessionID();
    }

    /** Get the EventType of this session */
    public EventType getType() {
        return data.getEventType();
    }

    /** Get the name of the map this session belongs to */
    public String getMapName() {
        return data.getMapName();
    }

    /** Get the tag of the map this session belongs to */
    public String getMapTag() {
        return data.getEventType().toString().toLowerCase() + "-" + data.getMapName();
    }

    /** Get the map this session belongs to */
    public EventMap getMap() {
        return map;
    }

    /** Get the time in seconds the session can run for max */
    public int getMaxTime() {
        return maxTime;
    }

    /** Get the session timer */
    public SessionTimer getTimer() {
        return timer;
    }

    /** Get the CWBoard for this session */
    public CWBoard getBoard() {
        return board;
    }

    /** Save the session to config */
    public void save() {
        if (session != null) {
            events.sessionCfg.setSession(getID(), data);
        }
    }

}
