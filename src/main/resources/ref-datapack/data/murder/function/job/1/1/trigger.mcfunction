execute as @e[tag=job_1_1_creeper_pos] at @e[tag=job_1_1] unless score @s sub_id = @e[sort=nearest,limit=1,tag=job_1_1_creeper_pos] sub_id run tag @s add job_1_1_damage_source
execute unless entity @e[tag=job_1_1] run tag @e[tag=job_1_1_creeper_pos] add job_1_1_damage_source
execute at @e[tag=job_1_1_damage_source] run function murder:job/1/1/mark
execute as @e[tag=job_1_1_damage_source] run schedule function murder:job/1/1/to_be_cleared 2t append
advancement revoke @s only murder:job/1/1/hurt_by_creeper
#sche