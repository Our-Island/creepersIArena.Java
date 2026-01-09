execute if entity @s[advancements={shop:2=false}] run tag @s add shop_buy_2
execute if entity @s[advancements={shop:2=true}] unless score @s particle_choose matches 2 run tag @s add shop_choose_2

execute as @s[tag=shop_buy_2] run function shop:buy/2
execute as @s[tag=shop_choose_2] run function shop:choose/2