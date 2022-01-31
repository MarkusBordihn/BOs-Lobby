# Fishing Dimension Setup Script
# by Markus Bordihn
#
# Spawn Point: 8 51 8
# Structure Block: 17 45 32 (1 1 -48 relative)
# Structure Block Corner: 32 58 -17
#
tellraw @p {"text":"Fishing dimension data pack will been re-initialized!", "color":"gold", "bold":true}

# Force Load chunks in a 3 x 3 chunk area
forceload add -16 31 31 -16

# Add Fishing Base
setblock -16 46 -16 minecraft:structure_block{mode:"LOAD",name:"lobby:fishing_base"} replace
setblock -16 47 -16 minecraft:redstone_block

# Remove force loaded chunks
forceload remove all

# Force load chunk for the spawn point
forceload add 8 8
