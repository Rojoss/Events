package com.clashwars.events.events;

import com.clashwars.events.events.koh.Koh;

/**
 * Each event is registered here and a class instance is created for each event.
 */
public enum EventType {
    KOH(new Koh());


    private BaseEvent eventClass;

    EventType(BaseEvent eventClass) {
        this.eventClass = eventClass;
    }

    /** Get the main class for the event */
    public BaseEvent getEventClass() {
        return eventClass;
    }
}
