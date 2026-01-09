advancement revoke @s only job:6_wolong/1_trigger

execute at @s run playsound minecraft:entity.breeze.wind_burst player @a[distance=..15] ~ ~ ~ 1 0
scoreboard players set @s cd_1 5
scoreboard players set @s cd_1_t 20

function job:6_wolong/skill/1/motion1

function regeneration:stop