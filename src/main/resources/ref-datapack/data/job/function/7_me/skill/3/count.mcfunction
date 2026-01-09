scoreboard players remove @s job_7_3_time 1
execute at @s positioned ~ ~1.5 ~ run particle dripping_water ~ ~ ~ 0.15 0.05 0.15 1 1
execute store result storage ci:job/7/3 time int 0.05 run scoreboard players get @s job_7_3_time
execute as @s[scores={job_7_3_time=1..}] run function job:7_me/skill/3/xp with storage ci:job/7/3
execute as @s[scores={job_7_3_time=..0}] run function job:7_me/skill/3/clear_attributes
