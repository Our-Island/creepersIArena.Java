scoreboard players remove @e[tag=job_6_2] job_6_2_life 1
execute at @s at @a[distance=..5] run particle dust{color:[70,70,0],scale:0.7} ~ ~ ~ 0.3 0.3 0.3 0 2 force @a[distance=..15]
execute at @s at @a[distance=..5] run particle dust{color:[70,70,0],scale:0.7} ~ ~1 ~ 0.3 0.3 0.3 0 2 force @a[distance=..15]
execute at @s as @a[distance=..5] unless score @e[sort=nearest,limit=1,tag=job_6_2] team = @s team run effect give @s glowing 2 0 true
execute at @s as @a[distance=..5] unless score @e[sort=nearest,limit=1,tag=job_6_2] team = @s team run effect give @s slowness 2 0 true
execute at @s run playsound minecraft:block.beacon.activate player @a[distance=..15] ~ ~ ~ 1 1
execute as @e[scores={job_6_2_life=0}] run function job:6_wolong/skill/2/death
#sche-