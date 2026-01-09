tag @s remove shop_buy_1
execute if score @s wealth_gunpowder matches 10.. run tellraw @a [{"selector": "@s"},{ "text": " 购买了 ","color": "white"},{"text": "爱心粒子","color": "white","underlined": true}]
execute if score @s wealth_gunpowder matches 10.. run tellraw @s [{ "text": "再次点击选择 ","color": "white"},{"text": "爱心粒子","color": "white","underlined": true}]
execute if score @s wealth_gunpowder matches 10.. at @s run playsound minecraft:entity.arrow.hit_player player @a[distance=..15] ~ ~ ~ 1 1
execute if score @s wealth_gunpowder matches 10.. run advancement grant @s only shop:1

execute unless score @s wealth_gunpowder matches 10.. run tellraw @s [{ "text": "火药不足","color": "red"}]
execute unless score @s wealth_gunpowder matches 10.. at @s run playsound minecraft:block.note_block.didgeridoo player @s ~ ~ ~ 1 1

execute if score @s wealth_gunpowder matches 10.. run scoreboard players remove @s wealth_gunpowder 10