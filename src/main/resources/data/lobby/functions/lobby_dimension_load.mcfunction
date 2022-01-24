# Mining Dimension Setup Script
# by Markus Bordihn
#
# Spawn Point: 0 10 0
# Structure Block: ? ? ? (0 1 0 relative)
# Structure Block Corner: ? ? ?

tellraw @p {"text":"Lobby dimension data pack will been initialized!", "color":"gold", "bold":true}

# Force Load chunks in a 100 x 100 area
forceload add -50 -50 50 50

# Test Blocks
setblock 10 10 10 shulker_box

# Start Platform
setblock -2 9 -2 minecraft:structure_block{mode:"LOAD",name:"lobby:start_platform"} replace
setblock -2 10 -2 minecraft:redstone_block

# Remove force loaded chunks
forceload remove all
