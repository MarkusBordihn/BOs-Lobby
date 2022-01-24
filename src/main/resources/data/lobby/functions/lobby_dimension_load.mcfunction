# Mining Dimension Setup Script
# by Markus Bordihn
#
# Spawn Point: 0 10 0
# Structure Block: -17 8 -17 (1 1 1 relative)
# Structure Block Corner: 16 23 16
#
tellraw @p {"text":"Lobby dimension data pack will been re-initialized!", "color":"gold", "bold":true}

# Force Load chunks in a 100 x 100 area
forceload add -50 -50 50 50

# Start Platform
setblock -16 8 -16 minecraft:structure_block{mode:"LOAD",name:"lobby:lobby_base"} replace
setblock -16 9 -16 minecraft:redstone_block

# Remove force loaded chunks
forceload remove all
