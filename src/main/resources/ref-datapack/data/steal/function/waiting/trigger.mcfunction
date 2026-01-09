execute unless score $tool game_stage matches 1.. if score $tool mode matches 1 run function steal:waiting/bossbar
execute if score $tool game_stage matches 0..1 if score $tool mode matches 1 run function steal:waiting/need_number
execute if score $tool game_stage matches 0..1 if score $tool mode matches 1 run function steal:waiting/ready_number
#sche