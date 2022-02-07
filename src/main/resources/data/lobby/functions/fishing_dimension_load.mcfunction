# Fishing Dimension Setup Script
# by Markus Bordihn
#
# Spawn Point: 42 51 12
# Structure Block fishing_base_ship 0 38 -17
# Structure Block fishing_base_taverne: 32 42 -17
#
tellraw @p {"text":"Fishing dimension data pack will been re-initialized!", "color":"gold", "bold":true}

# Force load chunks for the structures
forceload add -32 48 96 -32

# Add Fishing Base
setblock 0 39 -17 minecraft:structure_block{mode: "LOAD", name: "lobby:fishing/fishing_base_ship"} replace
setblock 0 40 -17 minecraft:redstone_block

setblock 32 43 -17 minecraft:structure_block{mode: "LOAD", name: "lobby:fishing/fishing_base_taverne"} replace
setblock 32 44 -17 minecraft:redstone_block

# Add NPCs without AI
summon villager 47 52 1 {Rotation: [180f, 0f], NoAI: 1b, PersistenceRequired: 1b, Invulnerable: 1b, VillagerData: {profession: "fisherman", type: "swamp"}}

# Add decoration mobs
summon parrot 44 56 24 {Rotation: [180f, 0f], NoAI: 1b, PersistenceRequired: 1b, Invulnerable: 1b, Sitting: 1b, Variant: 0}
summon parrot 22 66 19 {Rotation: [90f, 0f], NoAI: 1b, PersistenceRequired: 1b, Invulnerable: 1b, Sitting: 1b, Variant: 1}
summon parrot 14 61 27 {Rotation: [90f, 0f], NoAI: 1b, PersistenceRequired: 1b, Invulnerable: 1b, Sitting: 1b, Variant: 2}
summon parrot 28 60 -7 {Rotation: [225f, 0f], NoAI: 1b, PersistenceRequired: 1b, Invulnerable: 1b, Sitting: 1b, Variant: 3}
summon parrot 24 56 5 {Rotation: [0f, 45f], NoAI: 1b, PersistenceRequired: 1b, Invulnerable: 1b, Sitting: 1b, Variant: 4}
summon cat 45 60 2 {Rotation: [0f, 0f], NoAI: 1b, PersistenceRequired: 1b, Invulnerable: 1b, Sitting: 1b}

# Remove force loaded chunks
forceload remove all

# Force load chunk for the spawn point
forceload add 42 12
