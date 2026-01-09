execute at @s run playsound minecraft:block.dispenser.dispense player @a[distance=..15] ~ ~ ~ 1 1
execute at @s run playsound minecraft:entity.ender_eye.launch player @a[distance=..15] ~ ~ ~ 1 0

execute as @s at @s anchored eyes positioned ^ ^ ^-1 run summon arrow ~ ~ ~ {Tags:["job_2_1_new","job_2_1"],HasBeenShot:1b,LeftOwner:1b,shake:0b,crit:0b,pickup:0b,damage:1.2,SoundEvent:"minecraft:entity.player.attack.crit"}
execute at @e[tag=job_2_1_new] run tp @e[tag=job_2_1_new] ~ ~ ~ facing entity @s

data modify entity @e[tag=job_2_1_new,limit=1] Owner set from entity @s UUID
data modify entity @e[tag=job_2_1_new,limit=1] Rotation set from entity @s Rotation
execute at @e[tag=job_2_1_new,limit=1] run summon marker ^ ^ ^1 {Tags:["job_2_1_marker"]}

execute as @e[tag=job_2_1_marker] store result score @s job_2_1_x run data get entity @s Pos[0] 100
execute as @e[tag=job_2_1_marker] store result score @s job_2_1_y run data get entity @s Pos[1] 100
execute as @e[tag=job_2_1_marker] store result score @s job_2_1_z run data get entity @s Pos[2] 100



execute store result score @e[tag=job_2_1_new,limit=1] job_2_1_x run data get entity @e[tag=job_2_1_new,limit=1] Pos[0] 100
execute store result score @e[tag=job_2_1_new,limit=1] job_2_1_y run data get entity @e[tag=job_2_1_new,limit=1] Pos[1] 100
execute store result score @e[tag=job_2_1_new,limit=1] job_2_1_z run data get entity @e[tag=job_2_1_new,limit=1] Pos[2] 100

scoreboard players operation @e[tag=job_2_1_marker,limit=1] job_2_1_x -= @e[tag=job_2_1_new,limit=1] job_2_1_x 
execute store result entity @e[tag=job_2_1_new,limit=1] Motion[0] double 0.03 run scoreboard players get @e[tag=job_2_1_marker,limit=1] job_2_1_x

scoreboard players operation @e[tag=job_2_1_marker,limit=1] job_2_1_y -= @e[tag=job_2_1_new,limit=1] job_2_1_y
execute store result entity @e[tag=job_2_1_new,limit=1] Motion[1] double 0.03 run scoreboard players get @e[tag=job_2_1_marker,limit=1] job_2_1_y

scoreboard players operation @e[tag=job_2_1_marker,limit=1] job_2_1_z -= @e[tag=job_2_1_new,limit=1] job_2_1_z 
execute store result entity @e[tag=job_2_1_new,limit=1] Motion[2] double 0.03 run scoreboard players get @e[tag=job_2_1_marker,limit=1] job_2_1_z






kill @e[tag=job_2_1_marker]
tag @e remove job_2_1_new
