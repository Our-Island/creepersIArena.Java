advancement revoke @s only job:4_blood_race/4_trigger

tag @s add job_4_4_now
execute at @s run playsound minecraft:entity.phantom.ambient player @a[distance=..15] ~ ~ ~ 1 0.8
scoreboard players set @s cd_4 4
scoreboard players set @s cd_4_t 20
effect give @s speed 2 5 true
effect give @s dolphins_grace 2 0 true
scoreboard players set @s job_4_4_time 8

function regeneration:stop