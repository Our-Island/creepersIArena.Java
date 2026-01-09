tag @s add ready_for_war
scoreboard players reset @s lobby_portal
execute store result storage lobby:portal/spawn map int 1.0 run scoreboard players get $tool map_now
function lobby:portal/random_tp_point with storage lobby:portal/spawn
effect give @s blindness 2 0 true
effect give @s glowing 2 0 true
effect give @s saturation infinite 0 true
effect give @s regeneration 2 20 true
effect give @s fire_resistance 2 0 true
clear @s