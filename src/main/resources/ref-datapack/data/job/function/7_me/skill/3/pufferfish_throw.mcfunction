execute store result score @s job_7_3_pufferfish_time run random value 20..50
data modify entity @s Rotation[0] set from storage ci:job/7/3 direction
data modify entity @s Rotation[1] set value -10
execute at @s run summon marker ^ ^ ^1 {Tags:["job_7_3_marker"]}

execute as @e[tag=job_7_3_marker] store result score @s job_7_3_x run data get entity @s Pos[0] 100
execute as @e[tag=job_7_3_marker] store result score @s job_7_3_y run data get entity @s Pos[1] 100
execute as @e[tag=job_7_3_marker] store result score @s job_7_3_z run data get entity @s Pos[2] 100



execute store result score @s job_7_3_x run data get entity @s Pos[0] 100
execute store result score @s job_7_3_y run data get entity @s Pos[1] 100
execute store result score @s job_7_3_z run data get entity @s Pos[2] 100

scoreboard players operation @e[tag=job_7_3_marker,limit=1] job_7_3_x -= @s job_7_3_x 
$execute store result entity @s Motion[0] double $(distance) run scoreboard players get @e[tag=job_7_3_marker,limit=1] job_7_3_x

scoreboard players operation @e[tag=job_7_3_marker,limit=1] job_7_3_y -= @s job_7_3_y
$execute store result entity @s Motion[1] double $(distance) run scoreboard players get @e[tag=job_7_3_marker,limit=1] job_7_3_y

scoreboard players operation @e[tag=job_7_3_marker,limit=1] job_7_3_z -= @s job_7_3_z 
$execute store result entity @s Motion[2] double $(distance) run scoreboard players get @e[tag=job_7_3_marker,limit=1] job_7_3_z


scoreboard players operation @s id = @a[tag=job_7_3_owner,limit=1] id
scoreboard players operation @s team = @a[tag=job_7_3_owner,limit=1] team



kill @e[tag=job_7_3_marker]
tag @e remove job_7_3_new
tag @a remove job_7_3_owner