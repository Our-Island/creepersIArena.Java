advancement revoke @s only job:7_me/2_trigger

scoreboard players set @s cd_2 7
scoreboard players set @s cd_2_t 20

execute as @s run function job:3_avenger/skill/2/check_3
execute at @s run playsound minecraft:entity.bat.loop player @a[distance=..15] ~ ~ ~ 1 2
tag @s add job_7_2_from
execute at @s as @a[distance=..4] unless score @s team = @a[tag=job_7_2_from,limit=1] team run effect give @s blindness 1 0 true
execute at @s as @a[distance=..4] unless score @s team = @a[tag=job_7_2_from,limit=1] team run effect give @s glowing 1 0 true
execute at @s as @a[distance=..4] unless score @s team = @a[tag=job_7_2_from,limit=1] team run particle elder_guardian ~ ~ ~ 0 0 0 1 5 force @s
execute at @s as @a[distance=..4] unless score @s team = @a[tag=job_7_2_from,limit=1] team anchored eyes run particle flash{color:0} ^ ^ ^0.1 0 0 0 0 1 force @s

tag @a remove job_7_2_from
effect give @s invisibility infinite 0 true

function regeneration:stop

execute at @s run particle dust{color: [0,0,0],scale:0.6} ~ ~1 ~ 0.5 0.8 0.5 0 50 force @a[distance=..30]
execute at @s run particle dust{color: [0,0,0],scale:0.4} ~ ~1 ~ 0.4 0.6 0.4 0 30 force @a[distance=..30]
execute at @s run particle dust{color: [0,0,0],scale:0.8} ~ ~1 ~ 0.6 1.0 0.6 0 40 force @a[distance=..30]

#标签应用同job-2-skill-4相同
tag @s add job_7_2_not_safe

execute at @s run playsound minecraft:entity.bat.loop player @a[distance=..15] ~ ~ ~ 1 2
scoreboard players set @s job_7_2_time 50
tag @s add no_particle

clear @s cod
clear @s leather_chestplate