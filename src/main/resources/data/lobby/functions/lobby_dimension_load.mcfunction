# Mining Dimension Setup Script
# by Markus Bordihn
#
# Spawn Point: 9 9 9
# Structure Block: -17 8 -17 (1 1 1 relative)
# Structure Block Corner: 16 23 16
#
tellraw @p {"text":"Lobby dimension data pack will been re-initialized!", "color":"gold", "bold":true}

# Force load chunks for the structures
forceload add -16 80 32 -64

# Add Lobby Base
setblock -16 2 -16 minecraft:structure_block{mode: "LOAD", name: "lobby:lobby/lobby_base_garden"} replace
setblock -16 3 -16 minecraft:redstone_block

setblock -16 2 -64 minecraft:structure_block{mode: "LOAD", name: "lobby:lobby/lobby_base_taverne"} replace
setblock -16 3 -64 minecraft:redstone_block

setblock -16 2 31 minecraft:structure_block{mode: "LOAD", name: "lobby:lobby/lobby_base_blossom_tree"} replace
setblock -16 3 31 minecraft:redstone_block

# Add NPCs without AI
summon wandering_trader 6 9 -43 {Rotation: [-90f, 0f], NoAI: 1b, PersistenceRequired: 1b, Invulnerable: 1b}

# Add decoration mobs
summon tropical_fish 15 7 59 {PersistenceRequired: 1b, Invulnerable: 1b}
summon tropical_fish 7 8 48 {PersistenceRequired: 1b, Invulnerable: 1b}

# Remove force loaded chunks
forceload remove all

# Force load chunk for the spawn point
forceload add 9 9
