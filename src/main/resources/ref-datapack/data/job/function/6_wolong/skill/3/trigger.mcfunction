


execute as @s[scores={job_6_3_times=3}] run function job:6_wolong/skill/3/3
execute as @s[scores={job_6_3_times=2}] run function job:6_wolong/skill/3/2
execute as @s[scores={job_6_3_times=..1}] run function job:6_wolong/skill/3/1
scoreboard players remove @s job_6_3_times 1










data modify entity @e[tag=!job_6_3,tag=!job_2_2,tag=!job_6_3_checked,limit=1,type=#arrows] pickup set value 0b
data modify entity @e[tag=!job_6_3,tag=!job_2_2,tag=!job_6_3_checked,limit=1,type=#arrows] damage set value 0.8d
data modify entity @e[tag=!job_6_3,tag=!job_2_2,tag=!job_6_3_checked,limit=1,type=#arrows] crit set value 0b
tag @e[tag=!job_6_3,tag=!job_2_2,tag=!job_6_3_checked,type=#arrows] add job_6_3
tag @e[tag=!job_6_3,tag=!job_2_2,tag=!job_6_3_checked,type=#arrows] add job_6_3_checked



function regeneration:stop