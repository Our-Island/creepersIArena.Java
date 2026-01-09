execute at @s run playsound minecraft:item.crossbow.shoot player @a[distance=..15] ~ ~ ~ 1 1.3

#清除之前的光灵箭
execute at @s at @e[type=spectral_arrow,distance=..600] if score @e[limit=1,sort=nearest] id = @s id run tag @e[sort=nearest,limit=1] add job_2_2_before
kill @e[tag=job_2_2_before]

execute as @s at @s anchored eyes positioned ^ ^ ^-1 run summon spectral_arrow ~ ~ ~ {Tags:["job_2_2_new","job_2_2"],damage:0.6d,SoundEvent:"minecraft:entity.player.attack.crit",HasBeenShot:1b,LeftOwner:1b,shake:0b,crit:0b,pickup:0b}
execute at @e[tag=job_2_2_new] run tp @e[tag=job_2_2_new] ~ ~ ~ facing entity @s



data modify entity @e[tag=job_2_2_new,limit=1] Owner set from entity @s UUID
data modify entity @e[tag=job_2_2_new,limit=1] Rotation set from entity @s Rotation
execute at @e[tag=job_2_2_new,limit=1] run summon marker ^ ^ ^1 {Tags:["job_2_2_marker"]}

execute as @e[tag=job_2_2_marker] store result score @s job_2_2_x run data get entity @s Pos[0] 100
execute as @e[tag=job_2_2_marker] store result score @s job_2_2_y run data get entity @s Pos[1] 100
execute as @e[tag=job_2_2_marker] store result score @s job_2_2_z run data get entity @s Pos[2] 100



execute store result score @e[tag=job_2_2_new,limit=1] job_2_2_x run data get entity @e[tag=job_2_2_new,limit=1] Pos[0] 100
execute store result score @e[tag=job_2_2_new,limit=1] job_2_2_y run data get entity @e[tag=job_2_2_new,limit=1] Pos[1] 100
execute store result score @e[tag=job_2_2_new,limit=1] job_2_2_z run data get entity @e[tag=job_2_2_new,limit=1] Pos[2] 100

scoreboard players operation @e[tag=job_2_2_marker,limit=1] job_2_2_x -= @e[tag=job_2_2_new,limit=1] job_2_2_x 
execute store result entity @e[tag=job_2_2_new,limit=1] Motion[0] double 0.03 run scoreboard players get @e[tag=job_2_2_marker,limit=1] job_2_2_x

scoreboard players operation @e[tag=job_2_2_marker,limit=1] job_2_2_y -= @e[tag=job_2_2_new,limit=1] job_2_2_y
execute store result entity @e[tag=job_2_2_new,limit=1] Motion[1] double 0.03 run scoreboard players get @e[tag=job_2_2_marker,limit=1] job_2_2_y

scoreboard players operation @e[tag=job_2_2_marker,limit=1] job_2_2_z -= @e[tag=job_2_2_new,limit=1] job_2_2_z 
execute store result entity @e[tag=job_2_2_new,limit=1] Motion[2] double 0.03 run scoreboard players get @e[tag=job_2_2_marker,limit=1] job_2_2_z

scoreboard players operation @e[tag=job_2_2_new] id = @s id

kill @e[tag=job_2_2_marker]
tag @e remove job_2_2_new

execute as @s at @s anchored eyes positioned ^-1 ^ ^-1 run summon spectral_arrow ~ ~ ~ {Tags:["job_2_2_new","job_2_2"],damage:0.6d,SoundEvent:"minecraft:entity.player.attack.crit",HasBeenShot:1b,LeftOwner:1b,shake:0b,crit:0b,pickup:0b}
execute at @e[tag=job_2_2_new] run tp @e[tag=job_2_2_new] ~ ~ ~ facing entity @s



data modify entity @e[tag=job_2_2_new,limit=1] Owner set from entity @s UUID
data modify entity @e[tag=job_2_2_new,limit=1] Rotation set from entity @s Rotation
execute at @e[tag=job_2_2_new,limit=1] run summon marker ^ ^ ^1 {Tags:["job_2_2_marker"]}

execute as @e[tag=job_2_2_marker] store result score @s job_2_2_x run data get entity @s Pos[0] 100
execute as @e[tag=job_2_2_marker] store result score @s job_2_2_y run data get entity @s Pos[1] 100
execute as @e[tag=job_2_2_marker] store result score @s job_2_2_z run data get entity @s Pos[2] 100



execute store result score @e[tag=job_2_2_new,limit=1] job_2_2_x run data get entity @e[tag=job_2_2_new,limit=1] Pos[0] 100
execute store result score @e[tag=job_2_2_new,limit=1] job_2_2_y run data get entity @e[tag=job_2_2_new,limit=1] Pos[1] 100
execute store result score @e[tag=job_2_2_new,limit=1] job_2_2_z run data get entity @e[tag=job_2_2_new,limit=1] Pos[2] 100

scoreboard players operation @e[tag=job_2_2_marker,limit=1] job_2_2_x -= @e[tag=job_2_2_new,limit=1] job_2_2_x 
execute store result entity @e[tag=job_2_2_new,limit=1] Motion[0] double 0.03 run scoreboard players get @e[tag=job_2_2_marker,limit=1] job_2_2_x

scoreboard players operation @e[tag=job_2_2_marker,limit=1] job_2_2_y -= @e[tag=job_2_2_new,limit=1] job_2_2_y
execute store result entity @e[tag=job_2_2_new,limit=1] Motion[1] double 0.03 run scoreboard players get @e[tag=job_2_2_marker,limit=1] job_2_2_y

scoreboard players operation @e[tag=job_2_2_marker,limit=1] job_2_2_z -= @e[tag=job_2_2_new,limit=1] job_2_2_z 
execute store result entity @e[tag=job_2_2_new,limit=1] Motion[2] double 0.03 run scoreboard players get @e[tag=job_2_2_marker,limit=1] job_2_2_z

scoreboard players operation @e[tag=job_2_2_new] id = @s id

kill @e[tag=job_2_2_marker]
tag @e remove job_2_2_new

execute as @s at @s anchored eyes positioned ^1 ^ ^-1 run summon spectral_arrow ~ ~ ~ {Tags:["job_2_2_new","job_2_2"],damage:0.6d,SoundEvent:"minecraft:entity.player.attack.crit",HasBeenShot:1b,LeftOwner:1b,shake:0b,crit:0b,pickup:0b}
execute at @e[tag=job_2_2_new] run tp @e[tag=job_2_2_new] ~ ~ ~ facing entity @s



data modify entity @e[tag=job_2_2_new,limit=1] Owner set from entity @s UUID
data modify entity @e[tag=job_2_2_new,limit=1] Rotation set from entity @s Rotation
execute at @e[tag=job_2_2_new,limit=1] run summon marker ^ ^ ^1 {Tags:["job_2_2_marker"]}

execute as @e[tag=job_2_2_marker] store result score @s job_2_2_x run data get entity @s Pos[0] 100
execute as @e[tag=job_2_2_marker] store result score @s job_2_2_y run data get entity @s Pos[1] 100
execute as @e[tag=job_2_2_marker] store result score @s job_2_2_z run data get entity @s Pos[2] 100



execute store result score @e[tag=job_2_2_new,limit=1] job_2_2_x run data get entity @e[tag=job_2_2_new,limit=1] Pos[0] 100
execute store result score @e[tag=job_2_2_new,limit=1] job_2_2_y run data get entity @e[tag=job_2_2_new,limit=1] Pos[1] 100
execute store result score @e[tag=job_2_2_new,limit=1] job_2_2_z run data get entity @e[tag=job_2_2_new,limit=1] Pos[2] 100

scoreboard players operation @e[tag=job_2_2_marker,limit=1] job_2_2_x -= @e[tag=job_2_2_new,limit=1] job_2_2_x 
execute store result entity @e[tag=job_2_2_new,limit=1] Motion[0] double 0.03 run scoreboard players get @e[tag=job_2_2_marker,limit=1] job_2_2_x

scoreboard players operation @e[tag=job_2_2_marker,limit=1] job_2_2_y -= @e[tag=job_2_2_new,limit=1] job_2_2_y
execute store result entity @e[tag=job_2_2_new,limit=1] Motion[1] double 0.03 run scoreboard players get @e[tag=job_2_2_marker,limit=1] job_2_2_y

scoreboard players operation @e[tag=job_2_2_marker,limit=1] job_2_2_z -= @e[tag=job_2_2_new,limit=1] job_2_2_z 
execute store result entity @e[tag=job_2_2_new,limit=1] Motion[2] double 0.03 run scoreboard players get @e[tag=job_2_2_marker,limit=1] job_2_2_z

scoreboard players operation @e[tag=job_2_2_new] id = @s id

kill @e[tag=job_2_2_marker]
tag @e remove job_2_2_new

scoreboard players remove @s job_2_2_time 1
scoreboard players set @s job_2_2_time_t 15
scoreboard players set @s cd_9 12
scoreboard players set @s cd_9_t 20
