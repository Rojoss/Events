package com.clashwars.events.util;

import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.events.State;
import com.clashwars.events.maps.EventMap;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Util {

    public static String formatMsg(String msg) {
        return CWUtil.integrateColor("&8[&4Events&8] &6" + msg);
    }

    /**
     * Update the sign of the specified map.
     * If a session is provided (if not null) then it will update the player count and state based of the session data.
     * It will only work if there is a sign at the sign block location.
     */
    public static void updateSign(EventMap map, GameSession session) {
        Block block = map.getBlock("sign");
        if (block == null || (block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN)) {
            return;
        }
        Sign sign = (Sign)block.getState();
        sign.setLine(0, CWUtil.integrateColor("&5[" + CWUtil.capitalize(map.getType().toString().toLowerCase()) + "]"));
        sign.setLine(1, CWUtil.integrateColor(map.getName()));
        if (session == null) {
            sign.setLine(2, CWUtil.integrateColor("&a0&8/&2" + map.getMaxPlayers()));
            if (map.isClosed() || !map.validateMap().isEmpty()) {
                sign.setLine(3, CWUtil.integrateColor(State.CLOSED.getSignText()));
            } else {
                sign.setLine(3, CWUtil.integrateColor(State.OPENED.getSignText()));
            }
        } else {
            if (session.getSpecPlayerSize() > 0) {
                sign.setLine(2, CWUtil.integrateColor("&a" + session.getPlayerCount(false) + "&8/&2" + map.getMaxPlayers() + " &d+" + session.getSpecPlayerSize()));
            } else {
                sign.setLine(2, CWUtil.integrateColor("&a" + session.getPlayerCount(false) + "&8/&2" + map.getMaxPlayers()));
            }
            sign.setLine(3, CWUtil.integrateColor(session.getState().getSignText()));
        }
        sign.update(true);
    }
}
