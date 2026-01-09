advancement revoke @s only job:4_blood_race/3_trigger

execute at @s run playsound minecraft:entity.phantom.bite player @a[distance=..15] ~ ~ ~ 1 1.6
scoreboard players set @s cd_3 9
scoreboard players set @s cd_3_t 20
effect give @s levitation 2 39 true
scoreboard players set @s job_4_3_time 5

function regeneration:stop