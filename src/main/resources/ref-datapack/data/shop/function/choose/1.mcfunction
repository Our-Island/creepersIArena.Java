tag @s remove shop_choose_1

scoreboard players set @s particle_choose 1
tellraw @s [{ "text": "成功选择了 ","color": "green"},{"text": "爱心粒子","color": "white","underlined": true}]
execute at @s run playsound minecraft:entity.player.levelup player @s ~ ~ ~ 1 2