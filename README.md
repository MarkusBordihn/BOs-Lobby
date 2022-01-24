# Lobby

Provides a easy to use lobby and optimized mining dimension for you and your friends.

## Features ⭐

- Easy to use
- Optimized mining dimension without mobs and additional items.
- Provides /lobby, /mining and /spawn commands for the players.
- Changed automatically the user game mode depending on the dimension.
- Customization over the config file and data files.

## Lobby Dimension

The lobby dimension is a place to hang out with friends or to enjoy some of the provided content (wip).
But you can build your own lobby as well outside of the original spawn area.

All players are automatically in the adventure game mode in the lobby.

## Mining Dimension

The mining dimension will be generated on the first load over the `/mining` command.
It will not include any treasure chest or any mobs, so it could be only use for mining.
You will start in a mining base which provides you some of the basic stuff.
To return to the mining base just use the `/mining` command.

![][mining_dimension]

## User Commands

- **/lobby** teleports you to the lobby
- **/mining** teleports you to the mining dimension
- **/spawn** teleports you to the overworld

## Customization

You can customize the lobby and mining dimension over data files.

### Lobby Customization

The lobby will be automatically in the game mode adventure.
Use the following data files to customize the lobby:

- data/lobby/dimension/lobby_dimension.json
- data/lobby/dimension_type/lobby_dimension.json
- data/lobby/functions/lobby_dimension_load.mcfunction
- data/worldgen/biome/biome_lobby.json

### Mining Customization

The optimization are parts of the code, you can use the following data files for customization:

- data/lobby/dimension/mining_dimension.json
- data/lobby/dimension_type/mining_dimension.json
- data/lobby/functions/mining_dimension_load.mcfunction

[mining_dimension]: examples/mining_dimension.png
