scoreboard players reset $tool choosejob_time
scoreboard players set $tool game_stage 3
execute as @a at @s run playsound minecraft:item.goat_horn.sound.0 player @s ~ ~ ~ 1 0.9
tag @a[scores={team=1..2}] add ready_for_war
function steal:module/choosejob/break_barrier
bossbar set ci:choosejob players
bossbar set ci:steal_time_count players @a
bossbar set ci:steal_time_count max 180
bossbar set ci:steal_time_count value 180
scoreboard players set $tool steal_time_count 180
scoreboard players set $tool steal_mine 0
tellraw @a [{"text": "第","color": "white"},{"score":{"name": "$tool","objective": "round"},"color": "yellow"},{"text":" / ","color": "white"},{ "text": "7","color": "aqua"},{"text":" 轮","color": "white"}]
clear @a