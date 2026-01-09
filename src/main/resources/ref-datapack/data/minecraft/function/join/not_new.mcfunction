scoreboard players reset @s join_test
function minecraft:join/scoreboard_reset

effect give @s saturation infinite 0 true
scoreboard players set @s team 0
scoreboard players reset @s lobby_portal
tag @s remove ready_for_war
clear @s
effect give @s instant_health 1 20 true
scoreboard players reset @s murder_time_to_reset
scoreboard players reset @s murder_last_source
scoreboard players reset @s murder_type
scoreboard players reset @s continued_kill_reset_time
scoreboard players reset @s continued_kill
scoreboard players reset @s regeneration_time
attribute @s max_health base set 20
scoreboard players set @s team_choose 0
xp set @s 0 levels
xp set @s 0 points

attribute @s max_absorption base set 0
attribute @s knockback_resistance base set 0


advancement revoke @s only murder:accident/2_direct_hit_source

scoreboard players set @s steal_ready 0
execute if score $tool mode matches 1 if score $tool game_stage matches 1.. run gamemode spectator @s
execute if score $tool mode matches 1 run tellraw @s [{"text": "☀ ","color": "gold"},{"text": "当前模式：偷窃对抗","color": "white"}]
execute if score $tool mode matches 1 unless score $tool game_stage matches 1.. run gamemode adventure @s
execute if score $tool mode matches 1 unless score $tool game_stage matches 1.. run team join unready @s
execute if score $tool mode matches 1 unless score $tool game_stage matches 1.. run tp @s 5000 69 5000 90 0
execute if score $tool mode matches 1 if score $tool game_stage matches 1.. run team join spectator @s
execute if score $tool mode matches 1 run tp @s @r[scores={team=1..2}]
execute if score $tool mode matches 1 run scoreboard players set @s choosejob_page -1
execute if score $tool mode matches 1 if score $tool game_stage matches 2 run bossbar set ci:choosejob players @a
execute if score $tool mode matches 1 if score $tool game_stage matches 3 run bossbar set ci:steal_time_count players @a
execute if score $tool mode matches 1 if score $tool game_stage matches 4..5 run bossbar set ci:celebration players @a
scoreboard players set @s steal_ready 0

execute if score $tool mode matches 0 run gamemode adventure @s 
execute if score $tool mode matches 0 run tellraw @s [{"text": "☀ ","color": "gold"},{"text": "当前模式：战场乱斗","color": "white"}]
execute if score $tool mode matches 0 run team join lobby @s
execute if score $tool mode matches 0 run tp @s 5000 69 5000 90 0
execute if score $tool mode matches 0 run scoreboard players set @s choosejob_page 1

execute unless score $tool game_stage matches 2 run bossbar set ci:choosejob players

#attribute modifiers
attribute @s movement_speed modifier remove mutation:1/speed