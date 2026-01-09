scoreboard players operation $lobby:portal/join_team_1_4 red_pop = $tool red_pop
scoreboard players operation $lobby:portal/join_team_1_4 blue_pop = $tool blue_pop
scoreboard players operation $lobby:portal/join_team_1_4 green_pop = $tool green_pop
scoreboard players operation $lobby:portal/join_team_1_4 yellow_pop = $tool yellow_pop
scoreboard players add $lobby:portal/join_team_1_4 green_pop 1

execute if score $tool least_pop matches 1 run scoreboard players add $lobby:portal/join_team_1_4 red_pop 1
execute if score $tool least_pop matches 2 run scoreboard players add $lobby:portal/join_team_1_4 blue_pop 1
execute if score $tool least_pop matches 3 run scoreboard players add $lobby:portal/join_team_1_4 green_pop 1
execute if score $tool least_pop matches 4 run scoreboard players add $lobby:portal/join_team_1_4 yellow_pop 1

execute if score $lobby:portal/join_team_1_4 green_pop > $lobby:portal/join_team_1_4 red_pop if score $lobby:portal/join_team_1_4 green_pop > $lobby:portal/join_team_1_4 blue_pop if score $lobby:portal/join_team_1_4 green_pop > $lobby:portal/join_team_1_4 yellow_pop run function lobby:portal/join_team_0
execute if score $lobby:portal/join_team_1_4 green_pop > $lobby:portal/join_team_1_4 red_pop if score $lobby:portal/join_team_1_4 green_pop > $lobby:portal/join_team_1_4 blue_pop if score $lobby:portal/join_team_1_4 green_pop > $lobby:portal/join_team_1_4 yellow_pop run tellraw @s [{"text": "队伍不平衡，已为你自动切换队伍"}]

execute if score $lobby:portal/join_team_1_4 green_pop <= $lobby:portal/join_team_1_4 red_pop run scoreboard players set @s team 4
execute if score $lobby:portal/join_team_1_4 green_pop <= $lobby:portal/join_team_1_4 red_pop run team join green
execute if score $lobby:portal/join_team_1_4 green_pop <= $lobby:portal/join_team_1_4 blue_pop run scoreboard players set @s team 4
execute if score $lobby:portal/join_team_1_4 green_pop <= $lobby:portal/join_team_1_4 blue_pop run team join green
execute if score $lobby:portal/join_team_1_4 green_pop <= $lobby:portal/join_team_1_4 yellow_pop run scoreboard players set @s team 4
execute if score $lobby:portal/join_team_1_4 green_pop <= $lobby:portal/join_team_1_4 yellow_pop run team join green

