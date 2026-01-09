scoreboard players set $tool game_stage 5
scoreboard players set $tool celebrate_time_count 10

bossbar set ci:celebration max 10
bossbar set ci:celebration value 10

execute as @a at @s run playsound entity.ender_dragon.death player @s ~ ~ ~ 0.7 1