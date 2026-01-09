scoreboard players remove @a[scores={job_5_2_time=0..}] job_5_2_time 1
execute at @a[scores={job_5_2_time=0..}] run particle ash ~ ~1 ~ 0.5 0.8 0.5 0.5 5 force @a[distance=..30]
execute as @a[scores={job_5_2_time=..0}] run function job:5_rock/skill/2/stop


#sche-