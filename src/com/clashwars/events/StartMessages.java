package com.clashwars.events;

import com.clashwars.cwcore.utils.CWUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Randomized countdown start messages like 'ready?, set..., GO!'
 */
public class StartMessages {

    private static List<StartMessage> startMessages = new ArrayList<StartMessage>();

    public static void populate() {
        startMessages.add(new StartMessage("Ready?", "Set...", "GO!"));
        startMessages.add(new StartMessage("Ready?", "Steady...", "GO!"));
        startMessages.add(new StartMessage("Lights...", "Camera...", "Action!"));
        startMessages.add(new StartMessage("On your mark...", "Get set...", "GO!"));
        startMessages.add(new StartMessage("Are you ready?", "Are you?!?!?", "You better be! GO!"));
        startMessages.add(new StartMessage("We starting yet?", "I think we are...", "GO!"));
        startMessages.add(new StartMessage("Mouse: CHECK!", "Keyboard: CHECK!", "GO!"));
        startMessages.add(new StartMessage("Preparados...", "listos...", "Fuera!"));
        startMessages.add(new StartMessage("Pronti?", "Partenza", "Via!"));
        startMessages.add(new StartMessage("Auf die Plätze", "fertig", "los!"));
        startMessages.add(new StartMessage("A vos marques", "Prêts?", "Partez!"));
        startMessages.add(new StartMessage("למקומות", "היכון", "!צא"));
        startMessages.add(new StartMessage("Op uw plaatsen", "klaar voor de start?", "AF!"));
        startMessages.add(new StartMessage("På era platser", "färdiga", "gå!"));
        startMessages.add(new StartMessage("На старт", "Внимание", "Марш!"));
        startMessages.add(new StartMessage("各就各位", "预备", "跑"));
    }

    /** Get a random list of 3 start messages.
     * [0]=go [1]=set [2]=ready
     */
    public static String[] getMessages() {
        return CWUtil.random(startMessages).messages;
    }

    public static class StartMessage {
        String[] messages = new String[3];

        public StartMessage(String msg3, String msg2, String msg1) {
            messages[0] = msg1;
            messages[1] = msg2;
            messages[2] = msg3;
        }
    }
}
