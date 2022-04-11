# Changelog for Bo's Lobby

## Note

This change log includes the summarized changes.
For the full changelog, please go to the [Git Hub History][history] instead.

### 2022.04.11

- Added larger lobby update with additional buildings and hidden dungeon maze.
- Prepared latest major update for 1.18.1 version.

### 2022.04.09

- Added fall and fire protection for the /spawn command to avoid issues with wrongly set world spawn point (adjustable over the config).
- Excluded op players from specific checks so that they keep flying between dimensions.
- Allows villagers to progress, but without moving around. (Set `NoAI: 1b` in the .mcfunction scripts to disable)

### 2022.02.09

- Added mob control config options for fishing and lobby dimension
- Added additional decoration mobs for the fishing dimension

### 2022.02.08

- Released 2.1.0 stable version with improved default teleport to lobby options.
- Released 2.0.1 stable version.
  NOTE: Please delete the existing config file and reset the dimensions (mining, fishing and lobby) to get the latest updates.

- Finalized structures and fixed spawn points.
- Added creative option for fishing dimension.

### 2022.02.07

- Released 2.0.0 alpha version for testing. Use it on your own risk.
- Added option to transfer player directly to spawn after login.
- Added finishing base structure.
- Added new lobby base structure.
- Adjusted spawn points and setup scripts.

### 2022.02.06

- Added command cool down for teleport to avoid server spam.
- Move text to translatable component to support other languages.
- Improved logging for dimension manager to get a list of all possible dimension in the log for easier troubleshooting of reported issue like #4.

### 2022.01.30

- Replaced custom dimension teleporter with built-in version to avoid errors with other mods which are using mixin's and not consider the custom teleporter.

### 2022.01.26

- Adding additional checks to reset game type from lobby dimension.
- Added builder list to provide automatically creative mod for specific users.
- Added removing spawner options to save additional resource for mining dimension.
- Fixed integrated server is not detecting data pack status correctly.

### 2022.01.25

- Fixed #1 (Dangerous Beds in Default Lobby)
- Added additional configuration options to allow mob spawning, bat spawning or mining chest inside the mining dimension.
- Fixed mapping issues for local multiplayer server.

### 2022.01.24

- Added Lobby data handler to avoid duplicated entities: …
- Structure Blocks are not replacing entities, so I need to make sure that data packs are only loaded once to avoid entity spam.
- Added example image from the lobby dimension.
- First working version for testing

[history]: https://github.com/MarkusBordihn/BOs-Lobby/commits/main
