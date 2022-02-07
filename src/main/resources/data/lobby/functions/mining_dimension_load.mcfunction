# Mining Dimension Setup Script
# by Markus Bordihn
#
# Spawn Point: 203 9 560
# Structure Block: 217 7 547 (-38 1 1 relative / 38 9 23 size)
# Structure Block Corner: 178 17 571
#
tellraw @p {"text":"Mining dimension data pack will been re-initialized!", "color":"gold", "bold":true}

# Force load chunks for the structures
forceload add 178 571 217 547

# Add Mining Base
setblock 179 8 548 minecraft:structure_block{mode: "LOAD", name: "lobby:mining/mining_base"} replace
setblock 179 9 548 minecraft:redstone_block

# Add NPCs without AI
summon villager 195 9 554 {Rotation: [-45f, 0f], NoAI: 1b, PersistenceRequired: 1b, Invulnerable: 1b, VillagerData: {profession: "farmer", type: "savanna"}}

# Remove force loaded chunks
forceload remove all

# Force load chunk for the spawn point
forceload add 203 560
