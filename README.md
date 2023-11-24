# Skywars
Basic Paper plugin for Skywars minigame

## Config
Configuration is handled by a [separate plugin](https://github.com/emdeann/paper-worldconfig). The WorldConfig plugin spits out a YAML file, which can be copied directly into the Skywars plugin directory.
The config.yml file also requires two extra fields: `Template: ''` and `Lobby: ''`. These correspond to the names of the world folders which contain your template world (unchanged skywars map), and your lobby world.

Optionally, use the `MaxGames` field to set a maximum number of concurrent games. This will default to 5.

If the plugin folder or config file (`plugins\Skywars\config.yml`) do not yet exist, simply run the server once with the plugin's JAR file in the plugins folder. 

## Playing a Game
Once everything is configured, simply use `/start` to start up a skywars game. This will send all players to a fresh map, and begin the countdown to game start. Once one player remains, the game will end and send all players back to the lobby.
