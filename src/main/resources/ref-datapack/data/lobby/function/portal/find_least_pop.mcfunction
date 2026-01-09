execute if score $tool green_pop <= $tool blue_pop if score $tool green_pop <= $tool red_pop if score $tool green_pop <= $tool yellow_pop run scoreboard players set $tool least_pop 4

execute if score $tool yellow_pop <= $tool blue_pop if score $tool yellow_pop <= $tool red_pop if score $tool yellow_pop <= $tool green_pop run scoreboard players set $tool least_pop 3

execute if score $tool blue_pop <= $tool red_pop if score $tool blue_pop <= $tool yellow_pop if score $tool blue_pop <= $tool green_pop run scoreboard players set $tool least_pop 2

execute if score $tool red_pop <= $tool blue_pop if score $tool red_pop <= $tool yellow_pop if score $tool red_pop <= $tool green_pop run scoreboard players set $tool least_pop 1