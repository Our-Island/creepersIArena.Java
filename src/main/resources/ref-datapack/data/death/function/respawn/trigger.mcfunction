function murder:trigger
scoreboard players reset @s death_trigger
function murder:tellraw
function death:reset
effect give @s saturation infinite 0 true
effect give @s invisibility 10 0 true

execute if score $tool mode matches 0 run scoreboard players set @s death_spawn_time 160
execute if score $tool mode matches 0 run scoreboard players set @s death_heart_time 0

execute if score $tool mode matches 0 run attribute @s max_health base set 4
execute if score $tool mode matches 0 run tp @s 10 0 10




execute if score $tool mode matches 1 run function death:respawn/mode_steal
execute if score $tool mode matches 0 run function death:respawn/mode_war
execute at @s run playsound minecraft:entity.wither.spawn player @s ~ ~ ~ 1 1.5