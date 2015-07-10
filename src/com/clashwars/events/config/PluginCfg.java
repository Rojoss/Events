package com.clashwars.events.config;

import com.clashwars.cwcore.config.internal.EasyConfig;
import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 * Main plugin configuration file with all general config settings.
 */
public class PluginCfg extends EasyConfig {

    public String SQL__PASS = "SECRET";

    public PluginCfg(String fileName) {
        this.setFile(fileName);
    }
}
