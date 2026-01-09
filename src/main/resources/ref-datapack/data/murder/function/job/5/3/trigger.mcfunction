scoreboard players set @s murder_type 5003000
tag @e[nbt={Warmup:-8},tag=job_5_3_fang] add job_5_3_murderer

scoreboard players operation @s murder_last_source = @e[tag=job_5_3_murderer] id

scoreboard players set @s murder_time_to_reset 600

execute at @a if score @p id = @s murder_last_source run scoreboard players operation @p job_5_3_last_target = @s id

advancement revoke @a only murder:job/5/3/hit_by_fangs

tag @e remove job_5_3_murderer
