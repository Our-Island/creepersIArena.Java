
execute unless score @s particle_choose matches 0 run tag @s add shop_choose_0

execute as @s[tag=shop_choose_0] run function shop:choose/0