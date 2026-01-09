execute if entity @s[advancements={shop:5=false}] run tag @s add shop_buy_5
execute if entity @s[advancements={shop:5=true}] unless score @s particle_choose matches 5 run tag @s add shop_choose_5

execute as @s[tag=shop_buy_5] run function shop:buy/5
execute as @s[tag=shop_choose_5] run function shop:choose/5