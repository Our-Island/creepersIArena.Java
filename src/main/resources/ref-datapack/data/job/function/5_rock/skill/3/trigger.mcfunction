advancement revoke @s only job:5_rock/3_trigger

execute as @s at @a[distance=..15,tag=ready_for_war] if score @p id = @s job_5_3_last_target run tag @s add job_5_3_pass
execute as @s at @a[distance=..15,tag=ready_for_war] if score @p id = @s job_5_3_last_target run tag @p add job_5_3_target
execute at @s[tag=!job_5_3_pass] run playsound minecraft:block.note_block.didgeridoo player @s ~ ~ ~ 1 1
scoreboard players set @s[tag=job_5_3_pass] cd_3 20
scoreboard players set @s[tag=job_5_3_pass] cd_3_t 20
execute at @s[tag=job_5_3_pass] run particle sculk_soul ~ ~1 ~ 0.7 1 0.7 0.2 25 force @a[distance=..30]
execute at @s[tag=job_5_3_pass] run playsound minecraft:entity.allay.death player @a[distance=..15] ~ ~ ~ 1 0
execute if entity @a[tag=job_5_3_pass] run scoreboard players operation @a[tag=job_5_3_target] job_5_3_from = @a[limit=1,tag=job_5_3_pass] id

data remove storage ci:job/5/3 y
execute if entity @a[tag=job_5_3_pass] as @a[tag=job_5_3_target] store result storage ci:job/5/3 y double 1.0 run data get entity @s Motion[1]
execute if data storage ci:job/5/3 {y:-0.0784000015258789} run tag @a[tag=job_5_3_target] add job_5_3_target_pass
execute if entity @a[tag=job_5_3_pass] as @a[tag=job_5_3_target] unless block ~ ~-0.2 ~ #job:2/3/safe run tag @s add job_5_3_target_pass

execute as @a[tag=job_5_3_target] run function job:5_rock/skill/3/pass

execute as @a[tag=job_5_3_target] at @s run playsound minecraft:entity.elder_guardian.curse player @s ~ ~ ~ 1 1

tag @a remove job_5_3_target_pass
tag @a remove job_5_3_pass
tag @a remove job_5_3_target

function regeneration:stop