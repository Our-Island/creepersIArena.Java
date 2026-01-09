kill @e[tag=portal_text_display]
fill 4986 69 5001 4986 67 4999 air destroy
scoreboard players set @a choosejob_page -1
forceload add -19953 -20048 -20048 -19953
time set midnight
scoreboard players set $tool game_stage 0
bossbar set ci:war players
scoreboard objectives setdisplay sidebar