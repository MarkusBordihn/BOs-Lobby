# Changelog for Bo's Lobby

## Note

This change log includes the summarized changes.
For the full changelog, please go to the [Git Hub History][history] instead.

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
