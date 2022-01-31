# Mining Dimension Setup Script
# by Markus Bordihn
#
# Spawn Point: 9 9 9
# Structure Block: -17 8 -17 (1 1 1 relative)
# Structure Block Corner: 16 23 16
#
tellraw @p {"text":"Lobby dimension data pack will been re-initialized!", "color":"gold", "bold":true}

# Force Load chunks in a 32 x 32 area
forceload add -32 -32 32 32

# Add Lobby Base
setblock -16 2 -16 minecraft:structure_block{mode:"LOAD",name:"lobby:lobby_base"} replace
setblock -16 3 -16 minecraft:redstone_block

# Remove force loaded chunks
forceload remove all

# Force load chunk for the spawn point
forceload add 9 9
