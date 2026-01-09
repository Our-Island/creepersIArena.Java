advancement revoke @s only job:6_wolong/2_trigger

execute at @s run playsound minecraft:block.lantern.place player @a[distance=..15] ~ ~ ~ 1 1
scoreboard players set @s cd_2 14
scoreboard players set @s cd_2_t 20

function job:6_wolong/skill/2/summon

function regeneration:stop