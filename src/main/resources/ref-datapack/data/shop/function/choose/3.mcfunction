tag @s remove shop_choose_3

scoreboard players set @s particle_choose 3
tellraw @s [{ "text": "成功选择了 ","color": "green"},{"text": "灵魂粒子","color": "white","underlined": true}]
execute at @s run playsound minecraft:entity.player.levelup player @s ~ ~ ~ 1 2