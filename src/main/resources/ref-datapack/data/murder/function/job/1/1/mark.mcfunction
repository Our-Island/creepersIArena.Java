scoreboard players set @s murder_type 1001000



#被队友炸的
execute if score @e[tag=job_1_1_damage_source,limit=1,sort=nearest] team = @s team run scoreboard players set @s murder_type 1001010

#炸到自己
execute if score @e[tag=job_1_1_damage_source,limit=1,sort=nearest] id = @s id run scoreboard players set @s murder_type 1001020


tag @s add job_1_1_murdered
#被其他队伍炸的
execute if score @s murder_type matches 1001000 run scoreboard players operation @s murder_last_source = @e[tag=job_1_1_damage_source,limit=1,sort=nearest] id
execute if score @s murder_type matches 1001000 as @a if score @s id = @e[tag=job_1_1_damage_source,limit=1] id run scoreboard players set @s murder_time_to_reset 600
tag @a remove job_1_1_murdered


