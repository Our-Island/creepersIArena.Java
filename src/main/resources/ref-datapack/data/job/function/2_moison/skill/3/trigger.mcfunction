advancement revoke @s only job:2_moison/3_trigger

tag @s add job_2_3_fail

execute at @s if entity @e[type=spectral_arrow,distance=..40] at @e[type=spectral_arrow,distance=..40] if score @e[limit=1,sort=nearest,type=spectral_arrow] id = @s id at @s run tag @s remove job_2_3_fail

execute at @s if entity @s[tag=job_2_3_fail] run playsound minecraft:block.note_block.didgeridoo player @s ~ ~ ~ 1 1

execute at @s at @e[type=spectral_arrow,distance=..40] if score @e[limit=1,sort=nearest] id = @s id run scoreboard players set @s cd_3 20
execute at @s at @e[type=spectral_arrow,distance=..40] if score @e[limit=1,sort=nearest] id = @s id run scoreboard players set @s cd_3_t 20
execute at @s at @e[type=spectral_arrow,distance=..40] if score @e[limit=1,sort=nearest] id = @s id run clear @s ender_eye

execute at @s at @e[type=spectral_arrow,distance=..40] if score @e[limit=1,sort=nearest] id = @s id run tag @e[sort=nearest,limit=1] add job_2_3_matched
execute at @s at @s run tag @e[sort=nearest,limit=1,tag=job_2_3_matched] add job_2_3_chosen
tag @e remove job_2_3_matched
execute at @s if entity @e[tag=job_2_3_chosen] positioned as @e[tag=job_2_3_chosen] run function job:2_moison/skill/3/tp

function regeneration:stop