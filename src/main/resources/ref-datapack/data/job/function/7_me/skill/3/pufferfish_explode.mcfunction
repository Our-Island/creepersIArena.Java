tag @s add job_7_3_explode_source

execute at @s as @a[distance=..5] run scoreboard players set @s job_7_3_distance 5
execute at @s as @a[distance=..4] run scoreboard players set @s job_7_3_distance 4
execute at @s as @a[distance=..3] run scoreboard players set @s job_7_3_distance 3
execute at @s as @a[distance=..2] run scoreboard players set @s job_7_3_distance 2
execute at @s as @a[distance=..1] run scoreboard players set @s job_7_3_distance 1
execute at @s run particle gust ~ ~ ~ 0 0 0 1 1 force @a[distance=..30]
execute at @s run playsound entity.puffer_fish.blow_up player @a[distance=..15] ~ ~ ~ 1 2

execute at @s run playsound minecraft:entity.puffer_fish.sting player @a[distance=..15] ~ ~ ~ 1 2
execute at @s run playsound minecraft:enchant.thorns.hit player @a[scores={job_7_3_distance=1..5}] ~ ~ ~ 100 1
execute at @s as @a[scores={job_7_3_distance=1..5}] unless score @s team = @e[tag=job_7_3_explode_source,limit=1] team run scoreboard players set @s murder_type 7003000
execute at @s as @a[scores={job_7_3_distance=1..5}] unless score @s team = @e[tag=job_7_3_explode_source,limit=1] team run scoreboard players operation @s murder_last_source = @e[tag=job_7_3_explode_source] id

execute at @s as @a[scores={job_7_3_distance=1..5}] unless score @s team = @e[tag=job_7_3_explode_source,limit=1] team run particle enchanted_hit ~ ~0.5 ~ 0.5 0.5 0.5 0 6 force @a[distance=..30]

execute at @s as @a[scores={job_7_3_distance=1..5}] unless score @s team = @e[tag=job_7_3_explode_source,limit=1] team run function job:7_me/skill/3/pufferfish_damage
tag @e remove job_7_3_explode_source
effect give @a[scores={job_7_3_distance=5}] glowing 1 0 true
effect give @a[scores={job_7_3_distance=4}] glowing 1 0 true
effect give @a[scores={job_7_3_distance=3}] glowing 2 0 true
effect give @a[scores={job_7_3_distance=2}] glowing 3 0 true
effect give @a[scores={job_7_3_distance=1}] glowing 4 0 true

scoreboard players reset @a job_7_3_distance

kill @s