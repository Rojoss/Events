package com.clashwars.events.events;

import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.events._catch.Catch;
import com.clashwars.events.events.koh.Koh;
import com.clashwars.events.events.race.Race;
import com.clashwars.events.events.smash.Smash;
import com.clashwars.events.events.spleef.Spleef;

import java.util.ArrayList;
import java.util.List;

/**
 * Each event is registered here and a class instance is created for each event.
 */
public enum EventType {
    KOH(new Koh()),
    SPLEEF(new Spleef()),
    RACE(new Race()),
    SMASH(new Smash()),
    CATCH(new Catch());


    private BaseEvent eventClass;

    EventType(BaseEvent eventClass) {
        this.eventClass = eventClass;
    }

    /** Get the main class for the event */
    public BaseEvent getEventClass() {
        return eventClass;
    }


    public static EventType fromString(String name) {
        name = name.toLowerCase().replace("_","");
        for (EventType e : values()) {
            if (e.toString().toLowerCase().replace("_", "").equals(name)) {
                return e;
            }
        }
        return null;
    }

    public static List<String> getEventNames() {
        List<String> list = new ArrayList<String>();
        for (EventType type : values()) {
            list.add(CWUtil.capitalize(type.toString().toLowerCase().replace("_","")));
        }
        return list;
    }
}
