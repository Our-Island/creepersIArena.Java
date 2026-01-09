execute at @a[scores={job_2_2_time_t=5}] run playsound minecraft:item.crossbow.quick_charge_1 player @a[distance=..15] ~ ~ ~ 1 1
execute as @a[scores={job_2_2_time=1..}] run scoreboard players remove @s job_2_2_time_t 1
execute as @a[scores={job_2_2_time_t=..0,job_2_2_type=1}] run function job:2_moison/skill/2/shoot_arrow
execute as @a[scores={job_2_2_time_t=..0,job_2_2_type=2}] run function job:2_moison/skill/2/shoot_spectral

#sche-