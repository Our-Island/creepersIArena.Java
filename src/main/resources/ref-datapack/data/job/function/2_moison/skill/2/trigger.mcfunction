advancement revoke @s only job:2_moison/2_trigger

scoreboard players set @s cd_2 20
scoreboard players set @s cd_2_t 20

execute as @s[scores={cd_9=1..}] run scoreboard players set @s job_2_2_type 1
execute as @s unless entity @s[scores={cd_9=1..}] run scoreboard players set @s job_2_2_type 2
scoreboard players set @s job_2_2_time 5
scoreboard players set @s job_2_2_time_t 5
effect give @s slowness 3 1 true
#5,20,35,50,65共计5次

function regeneration:stop