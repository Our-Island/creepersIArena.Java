execute as @s[tag=tools_spawnpoint] run tag @s add tools_spawnpoint_yes
execute as @s[tag=!tools_spawnpoint] run tag @s add tools_spawnpoint_no

execute as @s at @s run playsound minecraft:entity.arrow.hit_player player @s ~ ~ ~ 1 1

tellraw @s[tag=tools_spawnpoint_no] [{"text": "开启了出生点显示","color": "green"}]
tellraw @s[tag=tools_spawnpoint_yes] [{"text": "关闭了出生点显示","color": "red"}]

tag @s[tag=tools_spawnpoint_yes] remove tools_spawnpoint
tag @s[tag=tools_spawnpoint_yes] remove tools_spawnpoint_yes

tag @s[tag=tools_spawnpoint_no] add tools_spawnpoint
tag @s[tag=tools_spawnpoint_no] remove tools_spawnpoint_no