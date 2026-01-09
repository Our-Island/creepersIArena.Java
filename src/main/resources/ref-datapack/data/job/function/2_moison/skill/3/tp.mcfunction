tag @s add job_2_3_not_tped

effect give @s slow_falling 1 0 true
execute at @s run playsound minecraft:block.end_portal_frame.fill player @a[distance=..15] ~ ~ ~ 10 0.5
execute at @s run particle minecraft:end_rod ~ ~0.6 ~ 0.4 0.6 0.4 0 15 force @a[distance=..30]
# 本格 与 上方1
execute positioned as @e[tag=job_2_3_chosen] if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tp @s ~ ~ ~

execute positioned as @e[tag=job_2_3_chosen] if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tag @s remove job_2_3_not_tped

# 本格 与 下方1

execute positioned as @e[tag=job_2_3_chosen] if entity @s[tag=job_2_3_not_tped] if block ~ ~ ~ #job:2/3/safe if block ~ ~-1 ~ #job:2/3/safe run tp @s ~ ~ ~

execute positioned as @e[tag=job_2_3_chosen] if entity @s[tag=job_2_3_not_tped] if block ~ ~ ~ #job:2/3/safe if block ~ ~-1 ~ #job:2/3/safe run tag @s remove job_2_3_not_tped



# 本格x-0.5 与 上方1
execute positioned as @e[tag=job_2_3_chosen] if entity @s[tag=job_2_3_not_tped] positioned ~-0.5 ~ ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tp @s ~ ~ ~

execute positioned as @e[tag=job_2_3_chosen] if entity @s[tag=job_2_3_not_tped] positioned ~-0.5 ~ ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tag @s remove job_2_3_not_tped
# 本格x+0.5 与 上方1
execute positioned as @e[tag=job_2_3_chosen] if entity @s[tag=job_2_3_not_tped] positioned ~0.5 ~ ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tp @s ~ ~ ~

execute positioned as @e[tag=job_2_3_chosen] if entity @s[tag=job_2_3_not_tped] positioned ~0.5 ~ ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tag @s remove job_2_3_not_tped

# 本格z-0.5 与 上方1
execute positioned as @e[tag=job_2_3_chosen] if entity @s[tag=job_2_3_not_tped] positioned ~ ~ ~-0.5 if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tp @s ~ ~ ~

execute positioned as @e[tag=job_2_3_chosen] if entity @s[tag=job_2_3_not_tped] positioned ~ ~ ~-0.5 if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tag @s remove job_2_3_not_tped
# 本格z+0.5 与 上方1
execute positioned as @e[tag=job_2_3_chosen] if entity @s[tag=job_2_3_not_tped] positioned ~ ~ ~0.5 if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tp @s ~ ~ ~

execute positioned as @e[tag=job_2_3_chosen] if entity @s[tag=job_2_3_not_tped] positioned ~ ~ ~0.5 if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tag @s remove job_2_3_not_tped

# 本格x-0.5 与 下方1
execute positioned as @e[tag=job_2_3_chosen] if entity @s[tag=job_2_3_not_tped] positioned ~-0.5 ~ ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~-1 ~ #job:2/3/safe run tp @s ~ ~ ~

execute positioned as @e[tag=job_2_3_chosen] if entity @s[tag=job_2_3_not_tped] positioned ~-0.5 ~ ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~-1 ~ #job:2/3/safe run tag @s remove job_2_3_not_tped
# 本格x+0.5 与 下方1
execute positioned as @e[tag=job_2_3_chosen] if entity @s[tag=job_2_3_not_tped] positioned ~0.5 ~ ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~-1 ~ #job:2/3/safe run tp @s ~ ~ ~

execute positioned as @e[tag=job_2_3_chosen] if entity @s[tag=job_2_3_not_tped] positioned ~0.5 ~ ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~-1 ~ #job:2/3/safe run tag @s remove job_2_3_not_tped

# 本格z-0.5 与 下方1
execute positioned as @e[tag=job_2_3_chosen] if entity @s[tag=job_2_3_not_tped] positioned ~ ~ ~-0.5 if block ~ ~ ~ #job:2/3/safe if block ~ ~-1 ~ #job:2/3/safe run tp @s ~ ~ ~

execute positioned as @e[tag=job_2_3_chosen] if entity @s[tag=job_2_3_not_tped] positioned ~ ~ ~-0.5 if block ~ ~ ~ #job:2/3/safe if block ~ ~-1 ~ #job:2/3/safe run tag @s remove job_2_3_not_tped
# 本格z+0.5 与 下方1
execute positioned as @e[tag=job_2_3_chosen] if entity @s[tag=job_2_3_not_tped] positioned ~ ~ ~0.5 if block ~ ~ ~ #job:2/3/safe if block ~ ~-1 ~ #job:2/3/safe run tp @s ~ ~ ~

execute positioned as @e[tag=job_2_3_chosen] if entity @s[tag=job_2_3_not_tped] positioned ~ ~ ~0.5 if block ~ ~ ~ #job:2/3/safe if block ~ ~-1 ~ #job:2/3/safe run tag @s remove job_2_3_not_tped

#最后
execute positioned as @e[tag=job_2_3_chosen] if entity @s[tag=job_2_3_not_tped] run tp @s ~ ~ ~
execute at @s run playsound minecraft:entity.enderman.teleport player @a[distance=..30] ~ ~ ~ 1 1
tag @e remove job_2_3_chosen












execute at @s run playsound minecraft:block.ender_chest.open player @a[distance=..15] ~ ~ ~ 10 1.8