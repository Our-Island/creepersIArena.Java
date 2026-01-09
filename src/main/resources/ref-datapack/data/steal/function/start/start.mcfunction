scoreboard players reset $tool start_countdown
scoreboard players set $tool game_stage 1
scoreboard players set @a[scores={steal_ready=0}] team -1
team join spectator @a[scores={team=-1}]
gamemode spectator @a
tp @a -19977 120 -19977 135 55
title @a title [{"text": "观察地图","color": "gray"}]
execute as @a at @s run playsound minecraft:block.note_block.bell player @s ~ ~ ~ 1 1
bossbar set ci:waiting players
bossbar set ci:spectator players @a

function steal:start/fill_redstone

#队伍分配-直接对应选择
scoreboard players set @a[scores={team_choose=1,steal_ready=1}] team 1
scoreboard players set @a[scores={team_choose=2,steal_ready=1}] team 2
#随机
execute as @a[scores={team_choose=0,steal_ready=1}] run function steal:start/team/random_team
#调整
execute as @a[scores={team_choose=1..2,steal_ready=1}] run function steal:start/team/team_balance

scoreboard players set $tool spectator_time 11
bossbar set ci:spectator max 11
bossbar set ci:spectator value 11
bossbar set ci:spectator players @a

clear @a

scoreboard players set $red steal_win 0
scoreboard players set $blue steal_win 0
scoreboard players set $tool steal_mine 0
scoreboard players reset $tool round