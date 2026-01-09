scoreboard players remove $tool celebrate_time_count 1
execute store result bossbar ci:celebration value run scoreboard players get $tool celebrate_time_count
bossbar set ci:celebration players @a
execute as @a[limit=3,gamemode=adventure] run function steal:module/end/firework_trigger
execute if score $tool celebrate_time_count matches 0 if score $tool game_stage matches 4 run function steal:module/end/celebration_end
execute if score $tool celebrate_time_count matches 0 if score $tool game_stage matches 5 run function steal:module/end/game_end
#sche-s