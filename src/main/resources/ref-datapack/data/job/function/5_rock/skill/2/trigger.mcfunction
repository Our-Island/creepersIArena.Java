advancement revoke @s only job:5_rock/2_trigger

execute at @s run playsound minecraft:item.dye.use player @a[distance=..15] ~ ~ ~ 1 0
execute at @s run playsound minecraft:entity.breeze.wind_burst player @a[distance=..15] ~ ~ ~ 1 0
scoreboard players set @s cd_2 10
scoreboard players set @s cd_2_t 20
effect give @s jump_boost 7 7 true
scoreboard players set @s job_5_2_time 60
attribute @s knockback_resistance base set 100

function regeneration:stop