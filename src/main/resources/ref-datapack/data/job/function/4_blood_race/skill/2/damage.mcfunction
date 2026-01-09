attribute @s knockback_resistance base set 100
scoreboard players reset @s job_4_2_success
execute store success score @s job_4_2_success run damage @s 1 minecraft:generic by @e[tag=job_4_2_now,limit=1] from @e[tag=job_4_2_now,limit=1]
tag @s remove job_4_2_matched
effect give @s glowing 3 0 true
execute if entity @s[scores={job_4_2_success=1}] run effect give @a[tag=job_4_2_master] strength 3 0
execute if entity @s[scores={job_4_2_success=1}] run scoreboard players add @a[tag=job_4_2_master] job_4_2_x 1
execute if entity @s[scores={job_4_2_success=1}] run xp add @a[tag=job_4_2_master] 1 levels
execute if entity @s[scores={job_4_2_success=1}] at @s run playsound minecraft:block.beacon.power_select player @a[distance=..15] ~ ~ ~ 1 2
execute if entity @s[scores={job_4_2_success=1}] run scoreboard players operation @s murder_last_source = @e[tag=job_4_2_now] id
execute if entity @s[scores={job_4_2_success=1}] run scoreboard players set @s murder_type 4002000



execute if entity @s[scores={job_4_2_success=1}] run scoreboard players set @s murder_time_to_reset 600
attribute @s knockback_resistance base set 0
