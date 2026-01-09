execute as @a[scores={death_spawn_time=160}] run attribute @s max_health base set 4
execute as @a[scores={death_spawn_time=140}] run attribute @s max_health base set 6
execute as @a[scores={death_spawn_time=120}] run attribute @s max_health base set 8
execute as @a[scores={death_spawn_time=100}] run attribute @s max_health base set 10
execute as @a[scores={death_spawn_time=80}] run attribute @s max_health base set 12
execute as @a[scores={death_spawn_time=60}] run attribute @s max_health base set 14
execute as @a[scores={death_spawn_time=40}] run attribute @s max_health base set 16
execute as @a[scores={death_spawn_time=20}] run attribute @s max_health base set 18
execute as @a[scores={death_spawn_time=0}] run attribute @s max_health base set 20
effect give @s resistance 2 5 true

execute as @s at @s run playsound minecraft:item.lead.tied player @s ~ ~ ~ 1 0
scoreboard players set @s death_heart_time 0
effect give @s instant_health 1 0 true

