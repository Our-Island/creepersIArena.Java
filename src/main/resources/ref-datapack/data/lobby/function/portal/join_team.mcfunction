function lobby:portal/find_least_pop
execute as @s[scores={team_choose=0}] run function lobby:portal/join_team_0
execute as @s[scores={team_choose=1}] run function lobby:portal/join_team_1
execute as @s[scores={team_choose=2}] run function lobby:portal/join_team_2
execute as @s[scores={team_choose=3}] run function lobby:portal/join_team_3
execute as @s[scores={team_choose=4}] run function lobby:portal/join_team_4

execute if entity @s[scores={team=1}] run tellraw @a [{"text": "✈ ","color": "red"},{"selector":"@s"},{"text": " 加入了战场","color": "white"}]
execute if entity @s[scores={team=2}] run tellraw @a [{"text": "✈ ","color": "blue"},{"selector":"@s"},{"text": " 加入了战场","color": "white"}]
execute if entity @s[scores={team=3}] run tellraw @a [{"text": "✈ ","color": "yellow"},{"selector":"@s"},{"text": " 加入了战场","color": "white"}]
execute if entity @s[scores={team=4}] run tellraw @a [{"text": "✈ ","color": "green"},{"selector":"@s"},{"text": " 加入了战场","color": "white"}]