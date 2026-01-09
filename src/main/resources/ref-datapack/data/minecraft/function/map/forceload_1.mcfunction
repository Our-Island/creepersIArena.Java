$forceload add $(pos_1_x) $(pos_1_z) $(pos_2_x) $(pos_2_z)


scoreboard players operation $map_load_x map_pos = $tool map_pos
scoreboard players operation $map_load_z map_pos = $tool map_pos

execute store result storage minecraft:map pos_2_x double 1.0 run scoreboard players get $map_load_x map_pos
execute store result storage minecraft:map pos_2_z double 1.0 run scoreboard players get $map_load_z map_pos

scoreboard players add $map_load_x map_pos 100
execute store result storage minecraft:map pos_1_x double 1.0 run scoreboard players get $map_load_x map_pos
scoreboard players remove $map_load_z map_pos 100
execute store result storage minecraft:map pos_1_z double 1.0 run scoreboard players get $map_load_z map_pos

function minecraft:map/forceload_2 with storage minecraft:map