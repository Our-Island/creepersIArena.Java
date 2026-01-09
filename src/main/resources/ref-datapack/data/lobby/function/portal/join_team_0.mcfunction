

execute if score $tool least_pop matches 1 run team join red @s
execute if score $tool least_pop matches 1 run scoreboard players set @s team 1

execute if score $tool least_pop matches 2 run team join blue @s
execute if score $tool least_pop matches 2 run scoreboard players set @s team 2

execute if score $tool least_pop matches 3 run team join yellow @s
execute if score $tool least_pop matches 3 run scoreboard players set @s team 3

execute if score $tool least_pop matches 4 run team join green @s
execute if score $tool least_pop matches 4 run scoreboard players set @s team 4
