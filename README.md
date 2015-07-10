# README #

Events plugin for the [ClashWars](http://clashwars.com) server.


## License ##

This plugin can't be compiled without the CWCore plugin which is a private repository.
The reason we don't make this other plugin public is to prevent random people from compiling the plugins and using it.
Feel free to use any of the code in this repository though.


## Contribution guidelines ##

You can contribute to this plugin by making pull requests.
The only important formatting rule is to place the brackets in line instead of placing them on a new line.
Other than that the formatting doesn't really matter too much just keep it clean and organized.
Another important rule is to write simple javadoc comments above all methods and classes about what they do.


## Creating an event ##

If you'd like to add your own event to this plugin you can do so and it's not very hard.
* First of all you add the event to the EventType enum.
* Then you create a class for your event like Spleef.java and extend this from BaseEvent.
* Add the constructor to your class which calls super() like the other event classes do.
* Add setup options if your event needs them like for spleef we need a floor cuboid.
* Create event listeners for all the things that handle your game. (see below)
* Create another class for your event and name it EventSession where you replace the event with your event name. like SpleefSession. (For each map a session is created when players join it)
* Add an if statement inside the createSession method in SessionManager for the event.
* In this Session class you override everything from GameSession what you need to change. Like you might want the max time be more than 5 minutes.

### Adding event listeners ###
Event listeners should be created in the event specific class like Spleef, Koh etc.
In the listener you first need to filter the event as much as possible.
For example for spleef block break you first check if the block is a snow block.
After that you need to get the session of the arena/player.
This can be done by calling 
```
#!java

getSession(Location loc);
```
This will try and find a game session and the given location. (if none is found it returns null)
Then you validate the session by checking if it's an instanceof your created session like SpleefSession.
And you check if the session has the player that broke the block, then you check if the session is started and then you can do your implementation.
Here is an example of an event handler.


```
#!java

@EventHandler
private void blockBreak(BlockBreakEvent event) {
    Block block = event.getBlock();
    if (block.getType() != Material.SNOW_BLOCK) {
        return;
    }

    //Filter out the event as much as possible before calling getSession();
    //This will scan through all sessions their maps if the location is within that map.
    //So it's important that this doesn't get called too much.
    GameSession gs = getSession(block.getLocation());
    if (gs == null || !(gs instanceof SpleefSession)) {
        return;
    }

    //Check if the player is inside the session.
    Player player = event.getPlayer();
    if (!gs.hasPlayer(player)) {
        return;
    }

    //Check if the game is started. (if GameState == STARTED)
    if (!gs.isStarted()) {
        CWUtil.actionBar("&4&l", "&cThe game hasn't started");
        return;
    }

    event.setCancelled(false);
    //Spleef implementation!
} 
```