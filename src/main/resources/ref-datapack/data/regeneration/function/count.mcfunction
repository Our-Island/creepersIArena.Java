scoreboard players add @s regeneration_time 1
execute as @s[scores={regeneration_time=20}] at @s run effect give @s regeneration 2 0 false
execute as @s[scores={regeneration_time=40}] at @s run effect give @s regeneration 2 0 false
execute as @s[scores={regeneration_time=60}] at @s run effect give @s regeneration 2 1 false
execute as @s[scores={regeneration_time=80}] at @s run effect give @s regeneration 2 1 false
execute as @s[scores={regeneration_time=105}] at @s run effect give @s regeneration 2 2 false
execute as @s[scores={regeneration_time=130}] at @s run effect give @s regeneration 2 2 false
execute as @s[scores={regeneration_time=155}] at @s run effect give @s regeneration 2 3 false
execute as @s[scores={regeneration_time=180}] at @s run effect give @s regeneration 3 3 false
execute as @s[scores={regeneration_time=210}] at @s run effect give @s regeneration 3 4 false
execute as @s[scores={regeneration_time=240}] at @s run effect give @s regeneration 3 5 false
execute as @s[scores={regeneration_time=270}] at @s run effect give @s regeneration 3 6 false
execute as @s[scores={regeneration_time=300}] at @s run effect give @s regeneration 3 7 false
execute as @s[scores={regeneration_time=330}] at @s run effect give @s regeneration 3 8 false

execute as @s[scores={regeneration_time=20}] at @s run playsound minecraft:block.note_block.chime player @s ~ ~ ~ 20 0.2
execute as @s[scores={regeneration_time=40}] at @s run playsound minecraft:block.note_block.chime player @s ~ ~ ~ 20 0.9
execute as @s[scores={regeneration_time=60}] at @s run playsound minecraft:block.note_block.chime player @s ~ ~ ~ 20 1.2
execute as @s[scores={regeneration_time=80}] at @s run playsound minecraft:block.beacon.ambient player @s ~ ~ ~ 1.5 2
execute as @s[scores={regeneration_time=105}] at @s run playsound minecraft:block.beacon.ambient player @s ~ ~ ~ 1.25 2
execute as @s[scores={regeneration_time=130}] at @s run playsound minecraft:block.beacon.ambient player @s ~ ~ ~ 1.0 2
execute as @s[scores={regeneration_time=155}] at @s run playsound minecraft:block.beacon.ambient player @s ~ ~ ~ 0.75 2
execute as @s[scores={regeneration_time=180}] at @s run playsound minecraft:block.beacon.ambient player @s ~ ~ ~ 0.5 2
execute as @s[scores={regeneration_time=210}] at @s run playsound minecraft:block.beacon.ambient player @s ~ ~ ~ 0.4 2
execute as @s[scores={regeneration_time=240}] at @s run playsound minecraft:block.beacon.ambient player @s ~ ~ ~ 0.3 2
execute as @s[scores={regeneration_time=270}] at @s run playsound minecraft:block.beacon.ambient player @s ~ ~ ~ 0.25 2
execute as @s[scores={regeneration_time=300}] at @s run playsound minecraft:block.beacon.ambient player @s ~ ~ ~ 0.15 2
execute as @s[scores={regeneration_time=330}] at @s run playsound minecraft:block.beacon.ambient player @s ~ ~ ~ 0.1 2