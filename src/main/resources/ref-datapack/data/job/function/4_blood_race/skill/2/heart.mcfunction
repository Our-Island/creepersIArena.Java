scoreboard players reset @s job_4_2_heart_time
execute at @s run playsound minecraft:entity.evoker_fangs.attack player @a[distance=..15] ~ ~ ~ 1 0

#execute at @s positioned ~ ~1 ~ run particle dust 0.549 0 0 1 ~ ~ ~ 0.3 0.5 0.3 0 25 force @a[distance=..30]
execute at @s run particle dust{color: [0.55, 0.0, 0.0],scale:1} ~ ~ ~ 0.3 0.5 0.3 0 25 force @a[distance=..30]

effect give @s absorption 2 10 true
execute if score @s job_4_2_x matches 0 run attribute @s max_absorption base set 1
execute if score @s job_4_2_x matches 1 run attribute @s max_absorption base set 2
execute if score @s job_4_2_x matches 2 run attribute @s max_absorption base set 4
execute if score @s job_4_2_x matches 3 run attribute @s max_absorption base set 5
execute if score @s job_4_2_x matches 4 run attribute @s max_absorption base set 6
execute if score @s job_4_2_x matches 5 run attribute @s max_absorption base set 7
execute if score @s job_4_2_x matches 6 run attribute @s max_absorption base set 8
execute if score @s job_4_2_x matches 7 run attribute @s max_absorption base set 9
execute if score @s job_4_2_x matches 8 run attribute @s max_absorption base set 10
execute if score @s job_4_2_x matches 9 run attribute @s max_absorption base set 11
execute if score @s job_4_2_x matches 10.. run attribute @s max_absorption base set 12

scoreboard players reset @s job_4_2_x
xp add @s -10000 levels
effect clear @s absorption
scoreboard players set @s job_4_2_reset_time 200
