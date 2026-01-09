tag @s remove shop_choose_2

scoreboard players set @s particle_choose 2
tellraw @s [{ "text": "成功选择了 ","color": "green"},{"text": "水滴粒子","color": "white","underlined": true}]
execute at @s run playsound minecraft:entity.player.levelup player @s ~ ~ ~ 1 2