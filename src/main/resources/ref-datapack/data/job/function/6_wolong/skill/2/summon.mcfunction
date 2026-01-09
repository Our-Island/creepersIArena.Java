execute at @s anchored eyes run summon item ^ ^ ^0.5 {Item:{id:"minecraft:lantern",count:1},Invulnerable:1b,PickupDelay:300,NoGravity:1b,Tags:["job_6_2","job_6_2_new"],Glowing:1b}
scoreboard players operation @e[tag=job_6_2_new] team = @s team
team join red @e[scores={team=1},tag=job_6_2_new]
team join blue @e[scores={team=2},tag=job_6_2_new]
team join yellow @e[scores={team=3},tag=job_6_2_new]
team join green @e[scores={team=4},tag=job_6_2_new]
scoreboard players set @e[tag=job_6_2_new] job_6_2_life 100



tag @e remove job_6_2_new