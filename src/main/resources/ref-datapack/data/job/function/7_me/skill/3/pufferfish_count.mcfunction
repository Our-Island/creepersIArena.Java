scoreboard players remove @s job_7_3_pufferfish_time 1
scoreboard players operation @s job_7_3_pufferfish_time_temp = @s job_7_3_pufferfish_time
scoreboard players operation @s job_7_3_pufferfish_time_temp %= $5 tool_constant
execute if score @s job_7_3_pufferfish_time_temp matches 2 at @s run playsound entity.tnt.primed player @a[distance=..15] ~ ~ ~ 1 2
execute if score @s job_7_3_pufferfish_time_temp matches 2 at @s run particle ash ~ ~ ~ 0.2 0.2 0.2 0 6 force @a[distance=..30]

execute as @s[scores={job_7_3_pufferfish_time=..0}] run function job:7_me/skill/3/pufferfish_explode