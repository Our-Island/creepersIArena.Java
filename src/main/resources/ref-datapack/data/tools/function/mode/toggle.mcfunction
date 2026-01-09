execute if score $tool mode matches 0 run scoreboard players set $tmp_tool mode 0
execute if score $tool mode matches 1 run scoreboard players set $tmp_tool mode 1
execute if score $tool mode matches 1 run function steal:waiting/count_down/stop_countdown
tellraw @a [{"text": ""}]
execute if score $tmp_tool mode matches 0 run tellraw @a [{"text": "☀ ","color": "gold"},{"text": "管理员切换了模式，当前模式：偷窃对抗","color": "white"}]
execute if score $tmp_tool mode matches 1 run tellraw @a [{"text": "☀ ","color": "gold"},{"text": "管理员切换了模式，当前模式：地图乱斗","color": "white"}]

execute if score $tmp_tool mode matches 0 run scoreboard players set $tool mode 1
execute if score $tmp_tool mode matches 1 run scoreboard players set $tool mode 0

execute as @a at @s run playsound minecraft:entity.generic.explode player @s ~ ~ ~ 1 1

clear @a
scoreboard players set @a team_choose 0

execute if score $tmp_tool mode matches 0 run function tools:mode/toggle_steal
execute if score $tmp_tool mode matches 1 run function tools:mode/toggle_war

scoreboard players set @a team 0
team join lobby @a
execute positioned 5000 68 5000 run tp @a[distance=15..] 5000 68 5000 90 15
execute as @a run function death:reset
function job:0_common/entity_reset
execute as @a[scores={death_spawn_time=1..}] run function death:respawn/exit/trigger
effect give @a instant_health 1 10 true
scoreboard players set @a steal_ready 0
scoreboard players set $tool ready_number 0
