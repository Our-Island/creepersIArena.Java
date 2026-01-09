summon armor_stand 4988 67 5000 {CustomName:[{"text": "加入战场"}],CustomNameVisible:1b,Invisible:1b,Marker:1b,Invulnerable:1b,NoGravity:1b,Tags:["portal_text_display"]}
fill 4986 69 5001 4986 67 4999 nether_portal[axis=z] destroy
scoreboard players set @a choosejob_page 1
forceload remove -19953 -20048 -20048 -19953
time set day
scoreboard players reset $tool game_stage
bossbar set ci:waiting players
bossbar set ci:celebration players
bossbar set ci:choosejob players
bossbar set ci:steal_time_count players
bossbar set ci:spectator players
scoreboard objectives setdisplay sidebar kill_score