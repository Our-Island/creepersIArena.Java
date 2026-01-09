scoreboard players add @a[scores={job_4_2_time=0..}] job_4_2_time_t 1
execute as @a[scores={job_4_2_time_t=12}] run function job:4_blood_race/skill/2/laser

execute as @e[tag=job_4_2_marker] run function job:4_blood_race/skill/2/move
scoreboard players remove @a[scores={job_4_2_heart_time=0..}] job_4_2_heart_time 1
execute as @a[scores={job_4_2_heart_time=..0}] run function job:4_blood_race/skill/2/heart
execute as @a[scores={job_4_2_reset_time=0..}] run function job:4_blood_race/skill/2/reset_heart
#sche-