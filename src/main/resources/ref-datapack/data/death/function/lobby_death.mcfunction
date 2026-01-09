scoreboard players reset @s death_trigger

function death:reset
effect give @s saturation infinite 0 true
effect give @s invisibility 10 0 true


execute if score $tool mode matches 0 run attribute @s max_health base set 20
execute if score $tool mode matches 0 run tp @s 10 0 10




execute at @s run playsound minecraft:entity.wither.spawn player @s ~ ~ ~ 1 1.5