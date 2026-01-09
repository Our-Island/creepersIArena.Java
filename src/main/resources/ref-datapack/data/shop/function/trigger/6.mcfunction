execute if entity @s[advancements={shop:6=false}] run tag @s add shop_buy_6
execute if entity @s[advancements={shop:6=true}] unless score @s particle_choose matches 6 run tag @s add shop_choose_6

execute as @s[tag=shop_buy_6] run function shop:buy/6
execute as @s[tag=shop_choose_6] run function shop:choose/6