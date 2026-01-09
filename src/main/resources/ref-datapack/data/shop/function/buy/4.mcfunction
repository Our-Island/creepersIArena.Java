tag @s remove shop_buy_4
execute if score @s wealth_gunpowder matches 5.. if score @s wealth_tnt matches 1.. run tellraw @a [{"selector": "@s"},{ "text": " 购买了 ","color": "white"},{"text": "彩色药水粒子","color": "white","underlined": true}]
execute if score @s wealth_gunpowder matches 5.. if score @s wealth_tnt matches 1.. run tellraw @s [{ "text": "再次点击选择 ","color": "white"},{"text": "彩色药水粒子","color": "white","underlined": true}]
execute if score @s wealth_gunpowder matches 5.. if score @s wealth_tnt matches 1.. at @s run playsound minecraft:entity.arrow.hit_player player @a[distance=..15] ~ ~ ~ 50 1
execute if score @s wealth_gunpowder matches 5.. if score @s wealth_tnt matches 1.. run advancement grant @s only shop:4

execute unless score @s wealth_gunpowder matches 5.. run tellraw @s [{ "text": "火药不足","color": "red"}]
execute unless score @s wealth_tnt matches 1.. run tellraw @s [{ "text": "TNT不足","color": "red"}]

execute unless score @s wealth_gunpowder matches 5.. unless score @s wealth_tnt matches 1.. at @s run playsound minecraft:block.note_block.didgeridoo player @s ~ ~ ~ 50 1

execute if score @s wealth_gunpowder matches 5.. if score @s wealth_tnt matches 1.. run tag @s add shop_pass
scoreboard players remove @s[tag=shop_pass] wealth_gunpowder 5
scoreboard players remove @s[tag=shop_pass] wealth_tnt 1
tag @s remove shop_pass