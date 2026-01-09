
scoreboard players set $tool game_stage 0
tp @a 5000 69 5000 90 0
scoreboard players set @a team 0
scoreboard players set @a choosejob_page -1
bossbar set ci:celebration players
gamemode adventure @a
tag @a remove ready_for_war
clear @a
bossbar set ci:steal_time_count players
effect give @a weakness 3 255 true
effect give @a instant_health 1 10 true
execute as @a run function death:reset
effect give @a instant_health 1 14 true