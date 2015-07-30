package com.clashwars.events.commands;

import com.clashwars.cwcore.cuboid.Cuboid;
import com.clashwars.cwcore.cuboid.Selection;
import com.clashwars.cwcore.cuboid.SelectionStatus;
import com.clashwars.cwcore.debug.Debug;
import com.clashwars.cwcore.dependencies.CWWorldEdit;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.commands.internal.PlayerCmd;
import com.clashwars.events.events.EventType;
import com.clashwars.events.maps.EventMap;
import com.clashwars.events.player.CWPlayer;
import com.clashwars.events.setup.SetupOption;
import com.clashwars.events.setup.SetupType;
import com.clashwars.events.util.Util;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.util.io.file.FilenameException;
import com.sk89q.worldedit.world.DataException;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.primesoft.asyncworldedit.api.blockPlacer.IJobEntryListener;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SetupCmd extends PlayerCmd {

    @Override
    public void onCommand(final Player player, String[] args) {
        if (args.length < 1) {
            showHelp(player);
            return;
        }

        CWPlayer cwp = events.pm.getPlayer(player);
        if (cwp.inSession() && cwp.getSession() != null) {
            cwp.setSelectedEvent(cwp.getSession().getType());
            cwp.setSelectedMap(cwp.getSession().getMapName());
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 3) {
                player.sendMessage(Util.formatMsg("&cInvalid usage! &7/setup create {event} {mapName}"));
                return;
            }

            EventType eventType = EventType.fromString(args[1]);
            if (eventType == null) {
                player.sendMessage(Util.formatMsg("&cInvalid event specified!"));
                player.sendMessage(CWUtil.integrateColor("&4Events&8: &7" + CWUtil.implode(EventType.getEventNames(), "&8, &7")));
                return;
            }

            EventMap map = events.mm.getMap(eventType, args[2]);
            if (map != null) {
                player.sendMessage(Util.formatMsg("&cA map with this name already exists!"));
                return;
            }

            map = events.mm.createMap(eventType, args[2]);
            cwp.setSelectedEvent(eventType);
            cwp.setSelectedMap(map.getName());

            player.sendMessage(Util.formatMsg("&a&lNew map for " + eventType.toString().toLowerCase().replace("_"," ") + " created! &8(&7And selected&8)"));
            player.sendMessage(CWUtil.integrateColor("&7Use &a/setup validate &7to set the map up!"));
            return;
        }


        if (args[0].equalsIgnoreCase("list")) {
            player.sendMessage(CWUtil.integrateColor("&8===== &4&lAll event maps &8====="));
            for (EventType eventType : EventType.values()) {
                player.sendMessage(CWUtil.integrateColor("&6&l" + CWUtil.capitalize(eventType.toString().toLowerCase().replace("_", "")) + "&8&l: &7" + CWUtil.implode(events.mm.getMapNames(eventType), "&8, &7")));
            }
            return;
        }


        if (args[0].equalsIgnoreCase("select")) {
            if (args.length < 3) {
                player.sendMessage(Util.formatMsg("&cInvalid usage! &7/setup select {event} {mapName}"));
                return;
            }

            EventType eventType = EventType.fromString(args[1]);
            if (eventType == null) {
                player.sendMessage(Util.formatMsg("&cInvalid event specified!"));
                player.sendMessage(CWUtil.integrateColor("&4Events&8: &7" + CWUtil.implode(EventType.getEventNames(), "&8, &7")));
                return;
            }

            EventMap map = events.mm.getMap(eventType, args[2]);
            if (map == null) {
                player.sendMessage(Util.formatMsg("&cInvalid map specified!"));
                player.sendMessage(CWUtil.integrateColor("&4Maps&8: &7" + CWUtil.implode(events.mm.getMapNames(eventType), "&8, &7")));
                return;
            }

            cwp.setSelectedEvent(eventType);
            cwp.setSelectedMap(map.getName());
            player.sendMessage(Util.formatMsg("&6&lMap selected!" + " &6Event&8: &a" + eventType.toString().toLowerCase() + " &6Map&8: &a" + map.getName()));
            return;
        }


        if (args[0].equalsIgnoreCase("info")) {
            if (cwp.getSelectedEvent() == null || cwp.getSelectedMap() == null || events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap()) == null) {
                player.sendMessage(Util.formatMsg("&cNo map selected! &7Select one using &c/setup select&7!"));
                return;
            }
            EventMap map = events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap());


            player.sendMessage(CWUtil.integrateColor("&8===== &4&lMap Information &8====="));
            player.sendMessage(CWUtil.integrateColor("&6Event&8: &7" + map.getType().toString().toLowerCase().replace("_"," ")));
            player.sendMessage(CWUtil.integrateColor("&6Name&8: &7" + map.getName()));
            player.sendMessage(CWUtil.integrateColor("&6Authors&8: &7" + ((map.getAuthors() == null || map.getAuthors().length <= 0) ? "&7None set" : CWUtil.implode(map.getAuthors(), "&8, &7"))));
            player.sendMessage(CWUtil.integrateColor("&6Min slots&8: &7" + map.getMinPlayers() + " &6Max slots&8: &7" + map.getMaxPlayers() + " &6VIP slots&8: &7" + map.getVipSpots()));
            player.sendMessage(CWUtil.integrateColor("&6Closed&8: &7" + (map.isClosed() ? "&4closed" : "&aopened")));
            player.sendMessage(CWUtil.integrateColor("&6Valid&8: &7" + (map.isValid() ? "&avalid" : "&4invalid")));

            player.sendMessage(CWUtil.integrateColor("&8--- &7&lSetup &7---"));
            String locs = "";
            String blocks = "";
            String cubs = "";
            String multilocs = "";
            for (SetupOption setupOption : map.getType().getEventClass().getSetupOptions()) {
                if (setupOption.type == SetupType.LOCATION) {
                    if (map.getLocation(setupOption.name) == null) {
                        locs += "&4" + setupOption.name + "&8, ";
                    } else {
                        locs += "&a" + setupOption.name + "&8, ";
                    }
                } else if (setupOption.type == SetupType.BLOCK_LOC) {
                    if (map.getBlock(setupOption.name) == null) {
                        blocks += "&4" + setupOption.name + "&8, ";
                    } else {
                        blocks += "&a" + setupOption.name + "&8, ";
                    }
                } else if (setupOption.type == SetupType.CUBOID) {
                    if (map.getCuboid(setupOption.name) == null) {
                        cubs += "&4" + setupOption.name + "&8, ";
                    } else {
                        cubs += "&a" + setupOption.name + "&8, ";
                    }
                } else if (setupOption.type == SetupType.MULTI_LOC) {
                    if (map.getMultiLocs(setupOption.name).size() <= 0) {
                        multilocs += "&4" + setupOption.name + "&8, ";
                    } else {
                        multilocs += "&a" + setupOption.name + "&8, ";
                    }
                }
            }

            if (!locs.isEmpty()) {
                player.sendMessage(CWUtil.integrateColor("&6Locations&8: &7" + locs));
            }
            if (!blocks.isEmpty()) {
                player.sendMessage(CWUtil.integrateColor("&6Block locations&8: &7" + blocks));
            }
            if (!cubs.isEmpty()) {
                player.sendMessage(CWUtil.integrateColor("&6Cuboids&8: &7" + cubs));
            }
            if (!multilocs.isEmpty()) {
                player.sendMessage(CWUtil.integrateColor("&6Multi locations&8: &7" + multilocs));
            }
            return;
        }


        if (args[0].equalsIgnoreCase("validate")) {
            if (cwp.getSelectedEvent() == null || cwp.getSelectedMap() == null || events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap()) == null) {
                player.sendMessage(Util.formatMsg("&cNo map selected! &7Select one using &c/setup select&7!"));
                return;
            }
            EventMap map = events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap());

            String validateError = map.validateMap();
            if (validateError.isEmpty()) {
                player.sendMessage(Util.formatMsg("&a&lThis map is set up correctly!"));
            } else {
                player.sendMessage(Util.formatMsg("&4&lMap error&8&l: &c" + validateError));
            }
            return;
        }


        if (args[0].equalsIgnoreCase("loc")) {
            if (cwp.getSelectedEvent() == null || cwp.getSelectedMap() == null || events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap()) == null) {
                player.sendMessage(Util.formatMsg("&cNo map selected! &7Select one using &c/setup select&7!"));
                return;
            }
            EventMap map = events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap());

            if (args.length < 2) {
                player.sendMessage(Util.formatMsg("&cInvalid usage! &7/setup loc {name}"));
                return;
            }

            if (map.setLocation(args[1], player.getLocation())) {
                player.sendMessage(Util.formatMsg("&6Location &a" + args[1] + " set!"));
            } else {
                player.sendMessage(Util.formatMsg("&cInvalid name! &7This event doesn't require a location with the name " + args[1] + "!"));
            }
            return;
        }


        if (args[0].equalsIgnoreCase("block")) {
            if (cwp.getSelectedEvent() == null || cwp.getSelectedMap() == null || events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap()) == null) {
                player.sendMessage(Util.formatMsg("&cNo map selected! &7Select one using &c/setup select&7!"));
                return;
            }
            EventMap map = events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap());

            if (args.length < 2) {
                player.sendMessage(Util.formatMsg("&cInvalid usage! &7/setup block {name}"));
                return;
            }

            List<Block> blocks = player.getLastTwoTargetBlocks((Set<Material>)null, 5);
            if (blocks.get(1) == null || blocks.get(1).getType() == Material.AIR) {
                player.sendMessage(Util.formatMsg("&cNo block! &7Look at a block to set the location!"));
            }

            if (map.setblock(args[1], blocks.get(1))) {
                player.sendMessage(Util.formatMsg("&6Block location &a" + args[1] + " set! &8(&7" + blocks.get(1).getType() + "&8)"));
                if (args[1].equalsIgnoreCase("sign")) {
                    Util.updateSign(map, null);
                }
            } else {
                player.sendMessage(Util.formatMsg("&cInvalid name! &7This event doesn't require a block with the name " + args[1] + "!"));
            }
            return;
        }


        if (args[0].equalsIgnoreCase("cuboid")) {
            if (cwp.getSelectedEvent() == null || cwp.getSelectedMap() == null || events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap()) == null) {
                player.sendMessage(Util.formatMsg("&cNo map selected! &7Select one using &c/setup select&7!"));
                return;
            }
            EventMap map = events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap());

            if (args.length < 2) {
                player.sendMessage(Util.formatMsg("&cInvalid usage! &7/setup cuboid {name}"));
                return;
            }

            Selection selection = events.getCore().getSel();
            SelectionStatus status = selection.getStatus(player);
            if (status == SelectionStatus.NONE) {
                player.sendMessage(Util.formatMsg("&cNo cuboid selected! &7Use &c/cww &7to get the wand and select two points."));
                return;
            }

            if (status == SelectionStatus.POS2) {
                player.sendMessage(Util.formatMsg("&cInvalid cuboid! &7You are missing &cposition 1&7!"));
                return;
            }

            if (status == SelectionStatus.POS1) {
                player.sendMessage(Util.formatMsg("&cInvalid cuboid! &7You are missing &cposition 2&7!"));
                return;
            }

            Cuboid cuboid = selection.getSelection(player);
            if (cuboid == null) {
                player.sendMessage(Util.formatMsg("&cInvalid cuboid! &7Try selecting it again!"));
                return;
            }

            if (map.setCuboid(args[1], cuboid)) {
                player.sendMessage(Util.formatMsg("&6Cuboid &a" + args[1] + " set!"));
            } else {
                player.sendMessage(Util.formatMsg("&cInvalid name! &7This event doesn't require a cuboid with the name " + args[1] + "!"));
            }
            return;
        }


        if (args[0].equalsIgnoreCase("multiloc")) {
            if (cwp.getSelectedEvent() == null || cwp.getSelectedMap() == null || events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap()) == null) {
                player.sendMessage(Util.formatMsg("&cNo map selected! &7Select one using &c/setup select&7!"));
                return;
            }
            EventMap map = events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap());

            if (args.length < 2) {
                player.sendMessage(Util.formatMsg("&cInvalid usage! &7/setup multiloc {name} [ID]"));
                return;
            }

            if (!map.getType().getEventClass().hasSetupOption(SetupType.MULTI_LOC, args[1])) {
                player.sendMessage(Util.formatMsg("&cInvalid name! &7This event doesn't require a multi loc with the name " + args[1] + "!"));
                return;
            }

            if (args.length < 3) {
                HashMap<Integer, Location> locs = map.getMultiLocs(args[1]);
                player.sendMessage(CWUtil.integrateColor("&8===== &4&lMultiloc Locations &8====="));
                for (Map.Entry<Integer, Location> loc : locs.entrySet()) {
                    player.sendMessage(CWUtil.integrateColor("&6&l" + loc.getKey() + "&8: &7" + loc.getValue().getBlockX() + "&8,&7" + loc.getValue().getBlockY() + "&8,&7" + loc.getValue().getBlockZ()));
                }
                return;
            }

            int id = CWUtil.getInt(args[2]);
            if (id < 0) {
                player.sendMessage(Util.formatMsg("&cInvalid ID! &7Specify a positive number as ID."));
                return;
            }

            if (map.setMultiLoc(args[1], id, player.getLocation())) {
                player.sendMessage(Util.formatMsg("&6Location &a" + args[1] + " &a&l" + id + " &6set!"));
            } else {
                player.sendMessage(Util.formatMsg("&cInvalid name! &7This event doesn't require a multi loc with the name " + args[1] + "!"));
            }
            return;
        }


        if (args[0].equalsIgnoreCase("multicuboid")) {
            if (cwp.getSelectedEvent() == null || cwp.getSelectedMap() == null || events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap()) == null) {
                player.sendMessage(Util.formatMsg("&cNo map selected! &7Select one using &c/setup select&7!"));
                return;
            }
            EventMap map = events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap());

            if (args.length < 2) {
                player.sendMessage(Util.formatMsg("&cInvalid usage! &7/setup cuboid {name} [ID]"));
                return;
            }

            if (!map.getType().getEventClass().hasSetupOption(SetupType.MULTI_CUBOID, args[1])) {
                player.sendMessage(Util.formatMsg("&cInvalid name! &7This event doesn't require a multi cuboid with the name " + args[1] + "!"));
                return;
            }

            if (args.length < 3) {
                HashMap<Integer, Cuboid> cuboids = map.getMultiCuboids(args[1]);
                player.sendMessage(CWUtil.integrateColor("&8===== &4&lMultiCuboid Locations &8====="));
                for (Map.Entry<Integer, Cuboid> loc : cuboids.entrySet()) {
                    player.sendMessage(CWUtil.integrateColor("&6&l" + loc.getKey() + "&8: &eLoc1:&7" + loc.getValue().getMinX() + "&8,&7" + loc.getValue().getMinY() + "&8,&7" + loc.getValue().getMinZ()
                        + " &eLoc2:&7" + loc.getValue().getMaxX() + "&8,&7" + loc.getValue().getMaxY() + "&8,&7" + loc.getValue().getMaxZ()
                    ));
                }
                return;
            }

            int id = CWUtil.getInt(args[2]);
            if (id < 0) {
                player.sendMessage(Util.formatMsg("&cInvalid ID! &7Specify a positive number as ID."));
                return;
            }

            Selection selection = events.getCore().getSel();
            SelectionStatus status = selection.getStatus(player);
            if (status == SelectionStatus.NONE) {
                player.sendMessage(Util.formatMsg("&cNo cuboid selected! &7Use &c/cww &7to get the wand and select two points."));
                return;
            }

            if (status == SelectionStatus.POS2) {
                player.sendMessage(Util.formatMsg("&cInvalid cuboid! &7You are missing &cposition 1&7!"));
                return;
            }

            if (status == SelectionStatus.POS1) {
                player.sendMessage(Util.formatMsg("&cInvalid cuboid! &7You are missing &cposition 2&7!"));
                return;
            }

            Cuboid cuboid = selection.getSelection(player);
            if (cuboid == null) {
                player.sendMessage(Util.formatMsg("&cInvalid cuboid! &7Try selecting it again!"));
                return;
            }

            if (map.setMultiCuboid(args[1], id, cuboid)) {
                player.sendMessage(Util.formatMsg("&6Cuboid &a" + args[1] + " &a&l" + id + " &6set!"));
            } else {
                player.sendMessage(Util.formatMsg("&cInvalid name! &7This event doesn't require a multi cuboid with the name " + args[1] + "!"));
            }
            return;
        }


        if (args[0].equalsIgnoreCase("slots")) {
            if (cwp.getSelectedEvent() == null || cwp.getSelectedMap() == null || events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap()) == null) {
                player.sendMessage(Util.formatMsg("&cNo map selected! &7Select one using &c/setup select&7!"));
                return;
            }
            EventMap map = events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap());

            if (args.length < 3) {
                player.sendMessage(Util.formatMsg("&cInvalid usage! &7/setup slots {min|max|vip} "));
                return;
            }

            int amount = CWUtil.getInt(args[2]);
            if (amount < 0) {
                player.sendMessage(Util.formatMsg("&cInvalid amount! &7Specify a possitive numeric amount!"));
                return;
            }

            if (args[1].equalsIgnoreCase("min")) {
                if (amount < 2) {
                    player.sendMessage(Util.formatMsg("&cInvalid amount! &7Minimum player count has to be at least 2."));
                    return;
                }
                map.setMinPlayers(amount);
                player.sendMessage(Util.formatMsg("&6Minimum required players set to &a" +amount + "&6!"));
                return;
            }

            if (args[1].equalsIgnoreCase("max")) {
                if (amount < map.getMinPlayers()) {
                    player.sendMessage(Util.formatMsg("&cInvalid amount! &7Max slots can't be lower than the minimum."));
                    return;
                }
                map.setMaxPlayers(amount);
                if (events.sm.hasSession(map.getType(), map.getName())) {
                    Util.updateSign(map, events.sm.getSession(map.getType(), map.getName()));
                } else {
                    Util.updateSign(map, null);
                }
                player.sendMessage(Util.formatMsg("&6Maximum allowed players set to &a" + amount + "&6!"));
                return;
            }

            if (args[1].equalsIgnoreCase("vip")) {
                map.setVipSpots(amount);
                player.sendMessage(Util.formatMsg("&6Extra VIP slots set to &a" + amount + "&6!"));
                return;
            }

            player.sendMessage(Util.formatMsg("&cInvalid usage! &7/setup slots {min|max|vip} "));
            return;
        }


        if (args[0].equalsIgnoreCase("authors")) {
            if (cwp.getSelectedEvent() == null || cwp.getSelectedMap() == null || events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap()) == null) {
                player.sendMessage(Util.formatMsg("&cNo map selected! &7Select one using &c/setup select&7!"));
                return;
            }
            EventMap map = events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap());

            if (args.length < 2) {
                player.sendMessage(Util.formatMsg("&cInvalid usage! &7/setup authors {author1,author2,etc}"));
                return;
            }

            String[] authors = args[1].split(",");
            if (authors.length < 1) {
                map.setAuthors(new String[] {});
                player.sendMessage(Util.formatMsg("&6Author list cleared!"));
                return;
            }

            map.setAuthors(authors);
            player.sendMessage(Util.formatMsg("&6Author list set!"));
            return;
        }

        if (args[0].equalsIgnoreCase("name")) {
            if (cwp.getSelectedEvent() == null || cwp.getSelectedMap() == null || events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap()) == null) {
                player.sendMessage(Util.formatMsg("&cNo map selected! &7Select one using &c/setup select&7!"));
                return;
            }
            EventMap map = events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap());

            if (args.length < 2) {
                player.sendMessage(Util.formatMsg("&cInvalid usage! &7/setup name {newname}"));
                return;
            }

            events.mm.renameMap(cwp.getSelectedEvent(), cwp.getSelectedMap(), args[1]);
            player.sendMessage(Util.formatMsg("&6&lName changed to &a&l" + args[1] + "&6&l!"));
            return;
        }

        if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete")) {
            if (cwp.getSelectedEvent() == null || cwp.getSelectedMap() == null || events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap()) == null) {
                player.sendMessage(Util.formatMsg("&cNo map selected! &7Select one using &c/setup select&7!"));
                return;
            }
            EventMap map = events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap());

            events.mm.maps.remove(map.getTag());
            events.mapCfg.removeMap(map.getTag());
            map.getBlock("sign").setType(Material.AIR);
            player.sendMessage(Util.formatMsg("&6&lMap removed!"));
            return;
        }

        if (args[0].equalsIgnoreCase("save")) {
            if (cwp.getSelectedEvent() == null || cwp.getSelectedMap() == null || events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap()) == null) {
                player.sendMessage(Util.formatMsg("&cNo map selected! &7Select one using &c/setup select&7!"));
                return;
            }
            EventMap map = events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap());

            Cuboid mapCuboid = map.getCuboid("map");
            if (mapCuboid == null) {
                player.sendMessage(Util.formatMsg("&cNo 'map' cuboid set! &7You have to set a cuboid with the name 'map' before you can save it!"));
                return;
            }

            File mapsFolder = new File(events.getDataFolder(), "maps");
            mapsFolder.mkdir();
            try {
                CWWorldEdit.saveSchematic(mapCuboid.getMinLoc(), mapCuboid.getMaxLoc(), new File(mapsFolder, map.getTag()));
                player.sendMessage(Util.formatMsg("&6&lThe map has been &a&lsaved&6&l!"));
            } catch (FilenameException e) {
                player.sendMessage(Util.formatMsg("&cFailed at saving the map! &7No file could be created for the map schematic."));
            } catch (IOException e) {
                player.sendMessage(Util.formatMsg("&cFailed at saving the map! &7Couldn't save data to the schematic file."));
            } catch (com.sk89q.worldedit.data.DataException e) {
                player.sendMessage(Util.formatMsg("&cFailed at saving the map! &7The schematic file couldn't be saved."));
            }
            return;
        }

        if (args[0].equalsIgnoreCase("load")) {
            if (cwp.getSelectedEvent() == null || cwp.getSelectedMap() == null || events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap()) == null) {
                player.sendMessage(Util.formatMsg("&cNo map selected! &7Select one using &c/setup select&7!"));
                return;
            }
            EventMap map = events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap());

            Cuboid mapCuboid = map.getCuboid("map");
            if (mapCuboid == null) {
                player.sendMessage(Util.formatMsg("&cNo 'map' cuboid set! &7You have to set a cuboid with the name 'map' before you can load it!"));
                return;
            }

            File mapsFolder = new File(events.getDataFolder(), "maps");
            mapsFolder.mkdir();
            try {
                IJobEntryListener callback = new IJobEntryListener() {
                    @Override
                    public void jobStateChanged(JobEntry jobEntry) {
                        if (jobEntry.isTaskDone() && jobEntry.getStatus() == JobEntry.JobStatus.Done) {
                            player.sendMessage(Util.formatMsg("&6&lThe map has been &a&lloaded&6&l!"));
                        }
                    }
                };
                player.sendMessage(Util.formatMsg("&7Loading the map..."));
                CWWorldEdit.loadSchematicAsync(new File(mapsFolder, map.getTag()), mapCuboid.getMinLoc(), callback);
            } catch (FilenameException e) {
                player.sendMessage(Util.formatMsg("&cFailed at loading the map! &7No schematic file found. Did you save it?"));
            } catch (IOException e) {
                player.sendMessage(Util.formatMsg("&cFailed at loading the map! &7Couldn't load the schematic file."));
            } catch (com.sk89q.worldedit.data.DataException e) {
                player.sendMessage(Util.formatMsg("&cFailed at loading the map! &7Couldn't load data from the schematic file."));
            } catch (MaxChangedBlocksException e) {
                player.sendMessage(Util.formatMsg("&cFailed at loading the map! &7The schematic is too big!"));
            } catch (DataException e) {
                player.sendMessage(Util.formatMsg("&cFailed at loading the map! &7Couldn't load data from the schematic file."));
            }
            return;
        }

        showHelp(player);
    }

    private void showHelp(Player player) {
        player.sendMessage(CWUtil.integrateColor("&8===== &4&lSetup Commands &8====="));
        player.sendMessage(CWUtil.integrateColor("&6/setup create {event} {mapName} &8- &7Register a new map."));
        player.sendMessage(CWUtil.integrateColor("&6/setup list &8- &7List all maps per event."));
        player.sendMessage(CWUtil.integrateColor("&6/setup select {event} {mapname} &8- &7Select the given event/map."));
        player.sendMessage(CWUtil.integrateColor("&6/setup info &8- &7Get information about the event/map."));
        player.sendMessage(CWUtil.integrateColor("&6/setup save &8- &7Save the map so it can be fully reset/loaded."));
        player.sendMessage(CWUtil.integrateColor("&6/setup load &8- &7Load the map in from the last saved state."));
        player.sendMessage(CWUtil.integrateColor("&8--- &7&lMap setup &8---"));
        player.sendMessage(CWUtil.integrateColor("&6/setup validate &8- &7Validate if everything is set up properly."));
        player.sendMessage(CWUtil.integrateColor("&6/setup loc {name} &8- &7Set a location for selected event/map."));
        player.sendMessage(CWUtil.integrateColor("&6/setup block {name} &8- &7Set a block location for selected event/map."));
        player.sendMessage(CWUtil.integrateColor("&6/setup cuboid {name} &8- &7Set a cuboid for selected event/map."));
        player.sendMessage(CWUtil.integrateColor("&6/setup multiloc {name} [ID] &8- &7Modify multiloc locations."));
        player.sendMessage(CWUtil.integrateColor("&6/setup multicuboid {name} [ID] &8- &7Modify multicuboid locations."));
        player.sendMessage(CWUtil.integrateColor("&8--- &7&lMap properties &8---"));
        player.sendMessage(CWUtil.integrateColor("&6/setup slots {min|max|vip} {amount} &8- &7Set slot amounts."));
        player.sendMessage(CWUtil.integrateColor("&6/setup authors {author,author,etc..} &8- &7Set/get list of authors"));
        player.sendMessage(CWUtil.integrateColor("&6/setup name {newname} &8- &7Rename the selected map."));
        player.sendMessage(CWUtil.integrateColor("&7&oMost of the above commands work for the selected map!"));
    }
}
