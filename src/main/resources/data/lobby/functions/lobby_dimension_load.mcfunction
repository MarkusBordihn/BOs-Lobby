# Mining Dimension Setup Script
# by Markus Bordihn
#
# Spawn Point: 9 11 9
# Structure Block: -17 8 -17 (1 1 1 relative)
# Structure Block Corner: 16 23 16
#
tellraw @p {"text":"Lobby dimension data pack will been re-initialized!", "color":"gold", "bold":true}

# Force load chunks for the structures
forceload add -64 -64 32 32

# Add Lobby Base
setblock -16 2 -16 minecraft:structure_block{mode: "LOAD", name: "lobby:lobby/lobby_base_garden"} replace
setblock -16 3 -16 minecraft:redstone_block

setblock -16 2 -64 minecraft:structure_block{mode: "LOAD", name: "lobby:lobby/lobby_base_taverne"} replace
setblock -16 3 -64 minecraft:redstone_block

setblock -16 2 31 minecraft:structure_block{mode: "LOAD", name: "lobby:lobby/lobby_base_blossom_tree"} replace
setblock -16 3 31 minecraft:redstone_block

setblock 32 2 31 minecraft:structure_block{mode: "LOAD", name: "lobby:lobby/lobby_base_flower"} replace
setblock 32 3 31 minecraft:redstone_block

setblock 32 2 -17 minecraft:structure_block{mode: "LOAD", name: "lobby:lobby/lobby_base_flower_2"} replace
setblock 32 3 -17 minecraft:redstone_block

setblock 32 2 -64 minecraft:structure_block{mode: "LOAD", name: "lobby:lobby/lobby_base_flower_3"} replace
setblock 32 3 -64 minecraft:redstone_block

setblock -64 2 31 minecraft:structure_block{mode: "LOAD", name: "lobby:lobby/lobby_base_bergw"} replace
setblock -64 3 31 minecraft:redstone_block

setblock -64 2 -17 minecraft:structure_block{mode: "LOAD", name: "lobby:lobby/lobby_base_forest_mid"} replace
setblock -64 3 -17 minecraft:redstone_block

setblock -64 2 -64 minecraft:structure_block{mode: "LOAD", name: "lobby:lobby/lobby_base_forest"} replace
setblock -64 3 -64 minecraft:redstone_block

# Add wandering trader with AI but without moving
summon wandering_trader 6 10 -43 {Rotation: [-90f, 0f], NoAI: 0b, PersistenceRequired: 1b, Invulnerable: 1b, Attributes: [{Name: "generic.movement_speed", Base: 0d}]}

# Add NPCs with AI (to allow progressing) but without moving
summon villager 20 10 -26 {Rotation: [0f, 0f], NoAI: 0b, PersistenceRequired: 0b, Invulnerable: 1b, VillagerData: {profession: "cartographer"}, Attributes: [{Name: "generic.movement_speed", Base: 0d}]}
summon villager 28 10 -26 {Rotation: [0f, 0f], NoAI: 0b, PersistenceRequired: 0b, Invulnerable: 1b, VillagerData: {profession: "fletcher"}, Attributes: [{Name: "generic.movement_speed", Base: 0d}]}
summon villager 25 10 -16 {Rotation: [180f, 0f], NoAI: 0b, PersistenceRequired: 1b, Invulnerable: 1b, VillagerData: {profession: "shepherd"}, Attributes: [{Name: "generic.movement_speed", Base: 0d}]}
summon villager 16 10 -16 {Rotation: [180f, 0f], NoAI: 0b, PersistenceRequired: 1b, Invulnerable: 1b, VillagerData: {profession: "toolsmith"}, Attributes: [{Name: "generic.movement_speed", Base: 0d}]}
summon villager -7 10 -56 {Rotation: [-45f, 0f], NoAI: 0b, PersistenceRequired: 1b, Invulnerable: 1b, VillagerData: {profession: "mason"}, Attributes: [{Name: "generic.movement_speed", Base: 0d}]}
summon villager 19 10 -37 {Rotation: [-45f, 0f], NoAI: 0b, PersistenceRequired: 1b, Invulnerable: 1b, VillagerData: {profession: "farmer"}, Attributes: [{Name: "generic.movement_speed", Base: 0d}]}
summon villager 1 10 -34 {Rotation: [-45f, 0f], NoAI: 0b, PersistenceRequired: 1b, Invulnerable: 1b, VillagerData: {profession: "toolsmith"}, Attributes: [{Name: "generic.movement_speed", Base: 0d}]}
summon villager 19 10 -51 {Rotation: [180f, 10f], NoAI: 0b, PersistenceRequired: 1b, Invulnerable: 1b, VillagerData: {profession: "farmer"}, Attributes: [{Name: "generic.movement_speed", Base: 0d}]}

# Add decoration mobs
summon tropical_fish 15 8 59 {PersistenceRequired: 1b, Invulnerable: 1b, Variant: 65536}
summon tropical_fish -13 14 47 {PersistenceRequired: 1b, Invulnerable: 1b, Variant: 65536}
summon horse 16 10 -59 {NoAI: 1b, PersistenceRequired: 1b, Invulnerable: 1b}
summon cow 56 10 -23 {NoAI: 1b, PersistenceRequired: 1b, Invulnerable: 1b}
summon sheep 63 10 30 {NoAI: 1b, PersistenceRequired: 1b, Invulnerable: 1b}

# Remove force loaded chunks
forceload remove all
