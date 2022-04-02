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

# Add NPCs with AI but without movement
summon villager 47 52 1 {Rotation: [180f, 0f], NoAI: 0b, PersistenceRequired: 1b, Invulnerable: 1b, VillagerData: {profession: "fisherman", type: "swamp"}, Attributes: [{Name: "generic.movement_speed", Base: 0d}]}

# Add decoration mobs like parrots and cats
summon parrot 44 56 24 {Rotation: [180f, 0f], NoAI: 1b, PersistenceRequired: 1b, Invulnerable: 1b, Sitting: 1b, Variant: 0}
summon parrot 22 66 19 {Rotation: [90f, 0f], NoAI: 1b, PersistenceRequired: 1b, Invulnerable: 1b, Sitting: 1b, Variant: 1}
summon parrot 14 61 27 {Rotation: [90f, 0f], NoAI: 1b, PersistenceRequired: 1b, Invulnerable: 1b, Sitting: 1b, Variant: 2}
summon parrot 28 60 -7 {Rotation: [225f, 0f], NoAI: 1b, PersistenceRequired: 1b, Invulnerable: 1b, Sitting: 1b, Variant: 3}
summon parrot 24 56 5 {Rotation: [0f, 45f], NoAI: 1b, PersistenceRequired: 1b, Invulnerable: 1b, Sitting: 1b, Variant: 4}
summon cat 45 60 2 {Rotation: [0f, 0f], NoAI: 1b, PersistenceRequired: 1b, Invulnerable: 1b, Sitting: 1b}

# Add some additional fish for decoration
summon tropical_fish 25 48 0 {PersistenceRequired: 1b, Invulnerable: 1b, Variant: 65536}
summon tropical_fish 31 48 2 {PersistenceRequired: 1b, Invulnerable: 1b, Variant: 50660352}
summon tropical_fish 29 43 19 {PersistenceRequired: 1b, Invulnerable: 1b, Variant: 67371009}
summon tropical_fish 63 47 30 {PersistenceRequired: 1b, Invulnerable: 1b, Variant: 101253888}
summon tropical_fish 51 47 -13 {PersistenceRequired: 1b, Invulnerable: 1b, Variant: 16778497}
summon tropical_fish 28 47 -17 {PersistenceRequired: 1b, Invulnerable: 1b, Variant: 117441793}
summon pufferfish 23 48 -1 {PersistenceRequired: 1b, Invulnerable: 1b}
summon salmon 22 47 9 {PersistenceRequired: 1b, Invulnerable: 1b}
summon cod 28 47 9 {PersistenceRequired: 1b, Invulnerable: 1b}

# Remove force loaded chunks
forceload remove all
