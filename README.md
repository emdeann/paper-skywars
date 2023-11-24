# Minigames
Basic Paper plugin for minigames, currently implemented game features are chests and spawn locations (think Skywars or SG)
Includes handling for multiple concurrent games on one server, as well as an ingame waiting lobby

## Config
Configuration is handled by a [separate plugin](https://github.com/emdeann/paper-worldconfig). The WorldConfig plugin spits out a YAML file, which can be copied directly into the Skywars plugin directory.
The config.yml file also requires two extra fields: `Template: ''` and `Lobby: ''`. These correspond to the names of the world folders which contain your template world (unchanged skywars map), and your lobby world.

Optionally, use the `MaxGames` field to set a maximum number of concurrent games. This will default to 5.

If the plugin folder or config file (`plugins\Skywars\config.yml`) do not yet exist, simply run the server once with the plugin's JAR file in the plugins folder. 

## Playing a Game
Once everything is configured, use `/start [names]` to start up a game. Currently only one map at a time is supported - corresponding to the template folder.
A game will end once there is one player remaining, or when `/stopgame` is used from within the game.

You may also use `/queue`, which will automatically send players to a game server when the queue fills. 
