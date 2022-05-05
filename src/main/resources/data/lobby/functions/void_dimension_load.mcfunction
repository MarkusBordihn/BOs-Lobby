# Void Dimension Setup Script
# by Markus Bordihn
#
# Spawn Point: 0 4 0
#
tellraw @p {"text":"Void dimension data pack will been re-initialized!", "color":"gold", "bold":true}

# Force load chunks for the structures
forceload add -16 -16 16 16

# Add start platform
setblock -2 2 -2 minecraft:structure_block{mode: "LOAD", name: "lobby:misc/start_platform"} replace
setblock -2 3 -2 minecraft:redstone_block

# Remove force loaded chunks
forceload remove all
