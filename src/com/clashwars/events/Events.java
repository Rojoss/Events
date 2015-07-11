package com.clashwars.events;

import com.clashwars.cwcore.CWCore;
import com.clashwars.events.commands.Commands;
import com.clashwars.events.config.data.MapCfg;
import com.clashwars.events.config.data.PlayerCfg;
import com.clashwars.events.config.PluginCfg;
import com.clashwars.events.events.EventType;
import com.clashwars.events.events.SessionManager;
import com.clashwars.events.maps.MapManager;
import com.clashwars.events.player.PlayerManager;
import com.google.gson.Gson;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.util.logging.Logger;

public class Events extends JavaPlugin {

    private static Events instance;
    private CWCore cwcore;
    private Gson gson = new Gson();

    private Commands cmds;

    public PluginCfg pluginCfg;
    public PlayerCfg playerCfg;
    public MapCfg mapCfg;

    public PlayerManager pm;
    public EventManager em;
    public SessionManager sm;
    public MapManager mm;

    private final Logger log = Logger.getLogger("Minecraft");


    @Override
    public void onDisable() {
        log("disabled");
    }

    @Override
    public void onEnable() {
        instance = this;

        Plugin plugin = getServer().getPluginManager().getPlugin("CWCore");
        if (plugin == null || !(plugin instanceof CWCore)) {
            log("CWCore dependency couldn't be loaded!");
            setEnabled(false);
            return;
        }
        cwcore = (CWCore)plugin;

        pluginCfg = new PluginCfg("plugins/Events/Config.yml");
        pluginCfg.load();
        playerCfg = new PlayerCfg("plugins/Events/data/Players.yml");
        playerCfg.load();
        mapCfg = new MapCfg("plugins/Events/data/Maps.yml");
        mapCfg.load();

        pm = new PlayerManager(this);
        em = new EventManager(this);
        sm = new SessionManager(this);
        mm = new MapManager(this);

        registerEvents();

        cmds = new Commands(this);

        log("loaded successfully");
    }



    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return cmds.onCommand(sender, cmd, label, args);
    }

    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        //pm.registerEvents(new MainEvents(this), this);

        EventType[] events = EventType.values();
        for (EventType event : events) {
            pm.registerEvents(event.getEventClass(), this);
        }
    }


    public void log(Object msg) {
        log.info("[Events " + getDescription().getVersion() + "] " + msg.toString());
    }

    public void logError(Object msg) {
        log.severe("[Events " + getDescription().getVersion() + "] " + msg.toString());
    }

    public static Events inst() {
        return instance;
    }


    public Gson getGson() {
        return gson;
    }

}
