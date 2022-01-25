# Mining Dimension Setup Script
# by Markus Bordihn
#
# Spawn Point: 202 11 561
# Structure Block: 216 8 550 (-31 1 1 relative)
# Structure Block Corner: 185 16 570
#
tellraw @p {"text":"Mining dimension data pack will been re-initialized!", "color":"gold", "bold":true}

# Force Load chunks for the structure
forceload add 185 570 216 550

# Mining Base
setblock 185 9 550 minecraft:structure_block{mode:"LOAD",name:"lobby:mining_base"} replace
setblock 185 10 550 minecraft:redstone_block

# Remove force loaded chunks
forceload remove all
