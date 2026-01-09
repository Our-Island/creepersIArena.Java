advancement revoke @s only job:1_creeper/1_trigger
scoreboard players set @s cd_1 7
scoreboard players set @s cd_1_t 20
effect give @s slowness 1 3 true
execute as @s run function job:1_creeper/skill/1/throw
function regeneration:stop