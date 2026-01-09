advancement revoke @s only choosejob:team/exit_trigger

execute if entity @s[scores={team=1}] run tellraw @a [{"text": "☃ ","color": "red"},{"selector":"@s"},{"text": " 退出了战场","color": "white"}]
execute if entity @s[scores={team=2}] run tellraw @a [{"text": "☃ ","color": "blue"},{"selector":"@s"},{"text": " 退出了战场","color": "white"}]
execute if entity @s[scores={team=3}] run tellraw @a [{"text": "☃ ","color": "yellow"},{"selector":"@s"},{"text": " 退出了战场","color": "white"}]
execute if entity @s[scores={team=4}] run tellraw @a [{"text": "☃ ","color": "green"},{"selector":"@s"},{"text": " 退出了战场","color": "white"}]

tp @s 5000 68.1 5000 facing 4993 67.5 5000
scoreboard players reset @s death_spawn_time
scoreboard players set @s team 0
scoreboard players reset @s death_heart_time
attribute @s max_health base set 20
effect give @s instant_health 1 4 true
clear @s
team join lobby @s


effect give @s saturation infinite 0 true
execute at @s run playsound minecraft:block.wooden_door.close player @s ~ ~ ~ 1 1
