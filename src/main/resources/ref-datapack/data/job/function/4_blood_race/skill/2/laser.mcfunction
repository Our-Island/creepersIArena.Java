scoreboard players add @s job_4_2_time 1
scoreboard players set @s job_4_2_time_t 0

execute at @s anchored eyes positioned ^ ^ ^ run summon marker ~ ~ ~ {Tags:["job_4_2_marker","job_4_2_marker_new"]}
data modify entity @e[tag=job_4_2_marker_new,limit=1] Rotation set from entity @s Rotation
scoreboard players set @e[tag=job_4_2_marker_new] job_4_2_marker_life 35
execute at @s run playsound minecraft:entity.firework_rocket.blast player @a[distance=..15] ~ ~ ~ 1 1.7
scoreboard players operation @e[tag=job_4_2_marker_new] id = @s id
scoreboard players operation @e[tag=job_4_2_marker_new] team = @s team
















tag @e remove job_4_2_marker_new

execute as @s[scores={job_4_2_time=6}] run function job:4_blood_race/skill/2/final