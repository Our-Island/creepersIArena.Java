scoreboard players remove $tool spectator_time 1
execute if score $tool spectator_time matches 10 run function steal:start/spectator_center
execute if score $tool spectator_time matches 6 run function steal:start/spectator_side_1
execute if score $tool spectator_time matches 3 run function steal:start/spectator_side_2
execute store result bossbar ci:spectator value run scoreboard players get $tool spectator_time
execute if score $tool spectator_time matches 0 run function steal:start/spectator_end
#sche