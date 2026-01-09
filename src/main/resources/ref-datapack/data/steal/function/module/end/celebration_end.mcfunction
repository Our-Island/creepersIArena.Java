execute unless entity @a[scores={team=1..2}] run function steal:module/end/game_end
execute if entity @a[scores={team=1}] unless entity @a[scores={team=2}] run function steal:module/end/player_leave/red_win
execute if entity @a[scores={team=2}] unless entity @a[scores={team=1}] run function steal:module/end/player_leave/blue_win
execute if entity @a[scores={team=1}] if entity @a[scores={team=2}] run function steal:module/choosejob/start

bossbar set ci:celebration players
tag @a remove ready_for_war
