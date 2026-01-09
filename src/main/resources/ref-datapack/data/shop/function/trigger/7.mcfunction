execute if entity @s[advancements={shop:7=false}] run tag @s add shop_buy_7
execute if entity @s[advancements={shop:7=true}] unless score @s particle_choose matches 7 run tag @s add shop_choose_7

execute as @s[tag=shop_buy_7] run function shop:buy/7
execute as @s[tag=shop_choose_7] run function shop:choose/7