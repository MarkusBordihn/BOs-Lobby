# Mining Dimension Setup Script
# by Markus Bordihn
#
# Spawn Point: 194 22 563
#
tellraw @p {"text":"Mining dimension data pack will been re-initialized!", "color":"gold", "bold":true}

# Force load chunks for the structures
forceload add 195 564 163 532

# Add Mining Base
setblock 179 8 548 minecraft:structure_block{mode: "LOAD", name: "lobby:mining/mining_base"} replace
setblock 179 9 548 minecraft:redstone_block

# Add NPC with AI
summon villager 199 13 564 {Rotation: [-45f, 0f], NoAI: 0b, PersistenceRequired: 1b, Invulnerable: 1b, VillagerData: {profession: "farmer", type: "savanna"}}

# Remove force loaded chunks
forceload remove all
