#检测到达一定人数开始倒计时
execute if score $tool game_stage matches 0 if score $tool mode matches 1 unless score $tool start_countdown matches 0.. if score $tool ready_number >= $tool need_number run function steal:waiting/count_down/start_countdown

#检测人数不足停止计时
execute if score $tool game_stage matches 0 if score $tool mode matches 1 if score $tool start_countdown matches 0.. if score $tool ready_number < $tool need_number run function steal:waiting/count_down/stop_countdown



#sche