execute as @s at @s anchored eyes run summon creeper ~ ~0.9 ~ {Tags:["job_1_1_new","job_1_1"],ExplosionRadius:2b,attributes:[ { base:20000, id:"minecraft:max_health"}], Fuse:25,ignited:1b,Health:20000}
effect give @e[tag=job_1_1_new] instant_health 1 200 true
execute at @s run playsound minecraft:entity.firework_rocket.blast_far player @a[distance=..15] ~ ~ ~ 1 2

data modify entity @e[tag=job_1_1_new,limit=1] Rotation set from entity @s Rotation
execute at @e[tag=job_1_1_new,limit=1] run summon marker ^ ^ ^1 {Tags:["job_1_1_marker"]}

execute as @e[tag=job_1_1_marker] store result score @s job_1_1_x run data get entity @s Pos[0] 100
execute as @e[tag=job_1_1_marker] store result score @s job_1_1_y run data get entity @s Pos[1] 100
execute as @e[tag=job_1_1_marker] store result score @s job_1_1_z run data get entity @s Pos[2] 100



execute store result score @e[tag=job_1_1_new,limit=1] job_1_1_x run data get entity @e[tag=job_1_1_new,limit=1] Pos[0] 100
execute store result score @e[tag=job_1_1_new,limit=1] job_1_1_y run data get entity @e[tag=job_1_1_new,limit=1] Pos[1] 100
execute store result score @e[tag=job_1_1_new,limit=1] job_1_1_z run data get entity @e[tag=job_1_1_new,limit=1] Pos[2] 100

scoreboard players operation @e[tag=job_1_1_marker,limit=1] job_1_1_x -= @e[tag=job_1_1_new,limit=1] job_1_1_x 
execute store result entity @e[tag=job_1_1_new,limit=1] Motion[0] double 0.015 run scoreboard players get @e[tag=job_1_1_marker,limit=1] job_1_1_x

scoreboard players operation @e[tag=job_1_1_marker,limit=1] job_1_1_y -= @e[tag=job_1_1_new,limit=1] job_1_1_y
execute store result entity @e[tag=job_1_1_new,limit=1] Motion[1] double 0.015 run scoreboard players get @e[tag=job_1_1_marker,limit=1] job_1_1_y

scoreboard players operation @e[tag=job_1_1_marker,limit=1] job_1_1_z -= @e[tag=job_1_1_new,limit=1] job_1_1_z 
execute store result entity @e[tag=job_1_1_new,limit=1] Motion[2] double 0.015 run scoreboard players get @e[tag=job_1_1_marker,limit=1] job_1_1_z

execute at @s run summon marker ~ ~ ~ {Tags:["job_1_1_creeper_pos","job_1_1_creeper_pos_new"]}
scoreboard players add $tool sub_id 1
scoreboard players operation @e[tag=job_1_1_new,limit=1] sub_id = $tool sub_id
scoreboard players operation @e[tag=job_1_1_new,limit=1] id = @s id
scoreboard players operation @e[tag=job_1_1_creeper_pos_new,limit=1] id = @s id
scoreboard players operation @e[tag=job_1_1_creeper_pos_new,limit=1] sub_id = $tool sub_id
scoreboard players operation @e[tag=job_1_1_creeper_pos_new,limit=1] team = @s team

ride @e[tag=job_1_1_creeper_pos_new,limit=1] mount @e[tag=job_1_1_new,limit=1]


kill @e[tag=job_1_1_marker]
tag @e remove job_1_1_new
tag @e remove job_1_1_creeper_pos_new
