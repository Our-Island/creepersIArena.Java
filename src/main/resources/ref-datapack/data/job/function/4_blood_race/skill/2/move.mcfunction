tag @s add job_4_2_now
scoreboard players remove @s job_4_2_marker_life 1
execute at @s positioned ^ ^ ^0.7 run tp @s ~ ~ ~

#execute at @s positioned ^0.075 ^0.035 ^ run particle dust 0.824 0.953 0.31 0.4 ~ ~ ~ 0 0 0 0 1 force @a[distance=..30]
#execute at @s positioned ^-0.075 ^-0.02 ^ run particle dust 0.898 0.592 0.161 0.4 ~ ~ ~ 0 0 0 0 1 force @a[distance=..30]
#execute at @s positioned ^ ^0.055 ^ run particle dust 0.608 0.776 0 0.4 ~ ~ ~ 0 0 0 0 1 force @a[distance=..30]
#execute at @s positioned ^0.055 ^-0.055 ^ run particle dust 0.608 0.776 0 0.4 ~ ~ ~ 0 0 0 0 1 force @a[distance=..30]
#execute at @s positioned ^-0.055 ^-0.035 ^ run particle dust 0.682 0.157 0.027 0.4 ~ ~ ~ 0 0 0 0 1 force @a[distance=..30]

execute at @s positioned ^0.075 ^0.035 ^ run particle dust{color: [0.824, 0.953, 0.31],scale:0.4} ~ ~ ~ 0 0 0 0 1 force @a[distance=..30]
execute at @s positioned ^-0.075 ^-0.02 ^ run particle dust{color: [0.898, 0.592, 0.161],scale:0.4} ~ ~ ~ 0 0 0 0 1 force @a[distance=..30]
execute at @s positioned ^ ^0.055 ^ run particle dust{color: [0.608, 0.776, 0.0],scale:0.4} ~ ~ ~ 0 0 0 0 1 force @a[distance=..30]
execute at @s positioned ^0.055 ^-0.055 ^ run particle dust{color: [0.608, 0.776, 0.0],scale:0.4} ~ ~ ~ 0 0 0 0 1 force @a[distance=..30]
execute at @s positioned ^-0.055 ^-0.035 ^ run particle dust{color: [0.682, 0.157, 0.027],scale:0.4} ~ ~ ~ 0 0 0 0 1 force @a[distance=..30]


execute at @s as @a[scores={job_choose=4}] if score @s id = @e[tag=job_4_2_now,limit=1] id run tag @s add job_4_2_master

execute positioned as @s run tag @e[distance=..0.75,type=!marker] add job_4_2_matched
execute positioned as @s positioned ~ ~-2 ~ run tag @e[distance=..0.6,type=!marker] add job_4_2_matched
execute positioned as @s positioned ~ ~-1.75 ~ run tag @e[distance=..0.6,type=!marker] add job_4_2_matched
execute positioned as @s positioned ~ ~-1.5 ~ run tag @e[distance=..0.6,type=!marker] add job_4_2_matched
execute positioned as @s positioned ~ ~-1.25 ~ run tag @e[distance=..0.6,type=!marker] add job_4_2_matched
execute positioned as @s positioned ~ ~-1 ~ run tag @e[distance=..0.6,type=!marker] add job_4_2_matched
execute positioned as @s positioned ~ ~-0.75 ~ run tag @e[distance=..0.6,type=!marker] add job_4_2_matched
execute positioned as @s positioned ~ ~-0.5 ~ run tag @e[distance=..0.6,type=!marker] add job_4_2_matched
execute positioned as @s positioned ~ ~-0.25 ~ run tag @e[distance=..0.6,type=!marker] add job_4_2_matched

execute as @e[tag=job_4_2_matched] if score @s team = @e[tag=job_4_2_now,limit=1] team run tag @s remove job_4_2_matched


execute as @e[tag=job_4_2_matched] run function job:4_blood_race/skill/2/damage



tag @a remove job_4_2_master
tag @e remove job_4_2_now

execute at @s run playsound minecraft:ui.loom.select_pattern player @a[distance=..15] ~ ~ ~ 0.1 2

execute at @s[scores={job_4_2_marker_life=0}] run particle end_rod ^ ^ ^0.3 0 0 0 0 1 force @a[distance=..30]
execute if entity @s[scores={job_4_2_marker_life=0}] run kill @s