advancement revoke @s only job:1_creeper/3_trigger
scoreboard players set @s cd_3 20
scoreboard players set @s cd_3_t 20

execute as @s run function job:1_creeper/skill/3/summon
function regeneration:stop