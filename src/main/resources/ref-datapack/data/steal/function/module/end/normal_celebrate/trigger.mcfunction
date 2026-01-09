scoreboard players set $tool celebrate_time_count 5
scoreboard players set $tool game_stage 4
bossbar set ci:celebration max 5
bossbar set ci:celebration value 5
execute as @a at @s run playsound minecraft:entity.ender_dragon.growl player @s ~ ~ ~ 1 1