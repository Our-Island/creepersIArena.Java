#标签应用同job-2-skill-4相同
tag @s add job_3_2_not_safe
effect give @s slow_falling 1 0 true
effect give @s speed 4 0 true
execute at @s run particle flame ~ ~1 ~ 0.3 0.5 0.3 0.0001 10 force @a[distance=..30]
execute at @s run particle dust{color: [0.91, 0.494, 0.078],scale:0.6} ~ ~1 ~ 0.35 0.55 0.35 0 10 force @a[distance=..30]

execute at @s run playsound minecraft:block.campfire.crackle player @a[distance=..15] ~ ~ ~ 1 2
scoreboard players set @s job_3_2_time 120

#3.5-原地,~
execute at @s positioned ^ ^ ^3.5 if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tp @s ~ ~ ~
execute at @s positioned ^ ^ ^3.5 if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tag @s remove job_3_2_not_safe
#3.5-原地,~0.25
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^ ^ ^3.5 positioned ~ ~0.25 ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tp @s ~ ~ ~
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^ ^ ^3.5 positioned ~ ~0.25 ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tag @s remove job_3_2_not_safe
#3.5-原地,~0.5
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^ ^ ^3.5 positioned ~ ~0.5 ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tp @s ~ ~ ~
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^ ^ ^3.5 positioned ~ ~0.5 ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tag @s remove job_3_2_not_safe
#3.5-原地,~-0.25
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^ ^ ^3.5 positioned ~ ~-0.25 ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tp @s ~ ~ ~
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^ ^ ^3.5 positioned ~ ~-0.25 ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tag @s remove job_3_2_not_safe
#3.5-原地,~-0.5
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^ ^ ^3.5 positioned ~ ~-0.5 ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tp @s ~ ~ ~
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^ ^ ^3.5 positioned ~ ~-0.5 ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tag @s remove job_3_2_not_safe

#3.5-原地0.5,~
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^0.5 ^ ^3.5 positioned ~ ~ ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tp @s ~ ~ ~
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^0.5 ^ ^3.5 positioned ~ ~ ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tag @s remove job_3_2_not_safe
#3.5-原地0.5,~0.25
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^0.5 ^ ^3.5 positioned ~ ~0.25 ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tp @s ~ ~ ~
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^0.5 ^ ^3.5 positioned ~ ~0.25 ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tag @s remove job_3_2_not_safe
#3.5-原地0.5,~0.5
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^0.5 ^ ^3.5 positioned ~ ~0.5 ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tp @s ~ ~ ~
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^0.5 ^ ^3.5 positioned ~ ~0.5 ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tag @s remove job_3_2_not_safe
#3.5-原地0.5,~-0.25
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^0.5 ^ ^3.5 positioned ~ ~-0.25 ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tp @s ~ ~ ~
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^0.5 ^ ^3.5 positioned ~ ~-0.25 ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tag @s remove job_3_2_not_safe
#3.5-原地0.5,~-0.5
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^0.5 ^ ^3.5 positioned ~ ~-0.5 ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tp @s ~ ~ ~
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^0.5 ^ ^3.5 positioned ~ ~-0.5 ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tag @s remove job_3_2_not_safe

#3.5-原地-0.5,~
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^-0.5 ^ ^3.5 positioned ~ ~ ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tp @s ~ ~ ~
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^-0.5 ^ ^3.5 positioned ~ ~ ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tag @s remove job_3_2_not_safe
#3.5-原地-0.5,~0.25
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^-0.5 ^ ^3.5 positioned ~ ~0.25 ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tp @s ~ ~ ~
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^-0.5 ^ ^3.5 positioned ~ ~0.25 ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tag @s remove job_3_2_not_safe
#3.5-原地-0.5,~0.5
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^-0.5 ^ ^3.5 positioned ~ ~0.5 ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tp @s ~ ~ ~
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^-0.5 ^ ^3.5 positioned ~ ~0.5 ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tag @s remove job_3_2_not_safe
#3.5-原地-0.5,~0.-25
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^-0.5 ^ ^3.5 positioned ~ ~-0.25 ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tp @s ~ ~ ~
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^-0.5 ^ ^3.5 positioned ~ ~-0.25 ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tag @s remove job_3_2_not_safe
#3.5-原地-0.5,~0.5
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^-0.5 ^ ^3.5 positioned ~ ~-0.5 ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tp @s ~ ~ ~
execute if entity @s[tag=job_3_2_not_safe] at @s positioned ^-0.5 ^ ^3.5 positioned ~ ~-0.5 ~ if block ~ ~ ~ #job:2/3/safe if block ~ ~1 ~ #job:2/3/safe run tag @s remove job_3_2_not_safe

#
execute if entity @s[tag=job_3_2_not_safe] run function job:3_avenger/skill/2/check_3