# Fishing Dimension Setup Script
# by Markus Bordihn
#
# Spawn Point: 42 51 12
# Structure Block base_l: 32 42 -17
# Structure Block base_r 0 38 -17
#
tellraw @p {"text":"Fishing dimension data pack will been re-initialized!", "color":"gold", "bold":true}

# Force Load chunks in a 2 x 3 chunk area
forceload add -16 31 31 -16

# Add Fishing Base
setblock 0 39 -17 minecraft:structure_block{mode:"LOAD",name:"lobby:fishing_base_r"} replace
setblock 0 40 -17 minecraft:redstone_block

setblock 32 43 -17 minecraft:structure_block{mode:"LOAD",name:"lobby:fishing_base_l"} replace
setblock 32 44 -17 minecraft:redstone_block

# Remove force loaded chunks
forceload remove all

# Force load chunk for the spawn point
forceload add 8 8
