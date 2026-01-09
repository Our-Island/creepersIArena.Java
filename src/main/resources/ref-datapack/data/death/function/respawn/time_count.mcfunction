scoreboard players remove @a[scores={death_spawn_time=1..}] death_spawn_time 1
scoreboard players add @a[scores={death_heart_time=0..}] death_heart_time 1
execute as @a[scores={death_spawn_time=140}] run function tips:trigger
execute as @a[scores={death_spawn_time=60}] run function tips:trigger
execute as @a[scores={death_heart_time=20..}] run function death:respawn/heart_add
execute as @a[scores={death_spawn_time=..0}] run function death:respawn/respawn
execute as @a[scores={death_spawn_time=1..}] at @s run playsound minecraft:block.beacon.ambient player @s ~ ~ ~ 10 0.1
execute positioned 10 0 10 run tp @a[distance=100..,scores={death_heart_time=0..}] 10 0 10
#sche