execute if entity @s[advancements={shop:4=false}] run tag @s add shop_buy_4
execute if entity @s[advancements={shop:4=true}] unless score @s particle_choose matches 4 run tag @s add shop_choose_4

execute as @s[tag=shop_buy_4] run function shop:buy/4
execute as @s[tag=shop_choose_4] run function shop:choose/4