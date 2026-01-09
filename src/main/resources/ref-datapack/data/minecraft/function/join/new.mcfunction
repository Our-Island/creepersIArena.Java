tag @s add not_new
scoreboard players set @s wealth_gunpowder 0
scoreboard players set @s wealth_tnt 0
scoreboard players set @s choosejob_page 1
scoreboard players set @s job_choose 1
scoreboard players set @s cd_1 0
scoreboard players set @s cd_2 0
scoreboard players set @s cd_3 0
scoreboard players set @s cd_4 0
scoreboard players set @s cd_5 0
scoreboard players set @s cd_6 0
scoreboard players set @s cd_7 0
scoreboard players set @s cd_8 0
scoreboard players set @s cd_9 0

scoreboard players add $tool id 1
scoreboard players operation @s id = $tool id 


function minecraft:initial/first_time
function minecraft:join/not_new