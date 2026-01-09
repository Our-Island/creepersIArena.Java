function shop:particle/trigger_1s
execute if score $tool start_countdown matches 0.. run function steal:waiting/count_down/countdown
execute if score $tool mode matches 1 if score $tool game_stage matches 1 run function steal:start/spectator_count
execute if score $tool mode matches 1 if score $tool game_stage matches 2 run function steal:module/choosejob/count
execute if score $tool mode matches 1 if score $tool game_stage matches 4..5 run function steal:module/end/celebrate_time_count
execute if score $tool mode matches 1 if score $tool game_stage matches 3 run function steal:module/process/count
execute if score $tool mode matches 0 run function job:0_common/kill_score

schedule function minecraft:loop_1s 1s
#sche