execute if entity @s[advancements={shop:1=false}] run tag @s add shop_buy_1
execute if entity @s[advancements={shop:1=true}] unless score @s particle_choose matches 1 run tag @s add shop_choose_1

execute as @s[tag=shop_buy_1] run function shop:buy/1
execute as @s[tag=shop_choose_1] run function shop:choose/1