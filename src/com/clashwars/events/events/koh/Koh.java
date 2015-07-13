package com.clashwars.events.events.koh;

import com.clashwars.events.setup.SetupOption;
import com.clashwars.events.setup.SetupType;
import com.clashwars.events.events.BaseEvent;

public class Koh extends BaseEvent {

    public Koh() {
        super();
        setupOptions.add(new SetupOption(SetupType.CUBOID, "hill", "Area where capturing is triggered."));
    }

}
