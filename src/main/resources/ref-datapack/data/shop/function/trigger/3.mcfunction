execute if entity @s[advancements={shop:3=false}] run tag @s add shop_buy_3
execute if entity @s[advancements={shop:3=true}] unless score @s particle_choose matches 3 run tag @s add shop_choose_3

execute as @s[tag=shop_buy_3] run function shop:buy/3
execute as @s[tag=shop_choose_3] run function shop:choose/3