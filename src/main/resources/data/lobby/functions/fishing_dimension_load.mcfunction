# Fishing Dimension Setup Script
# by Markus Bordihn
#
# Spawn Point: 42 51 12
# Structure Block fishing_base_ship 0 38 -17
# Structure Block fishing_base_taverne: 32 42 -17
#
tellraw @p {"text":"Fishing dimension data pack will been re-initialized!", "color":"gold", "bold":true}

# Force load chunks for the structures
forceload add 0 32 64 -16

# Add Fishing Base
setblock 0 39 -17 minecraft:structure_block{mode:"LOAD",name:"lobby:fishing/fishing_base_ship"} replace
setblock 0 40 -17 minecraft:redstone_block

setblock 32 43 -17 minecraft:structure_block{mode:"LOAD",name:"lobby:fishing/fishing_base_taverne"} replace
setblock 32 44 -17 minecraft:redstone_block

# Remove force loaded chunks
forceload remove all

# Force load chunk for the spawn point
forceload add 42 12
