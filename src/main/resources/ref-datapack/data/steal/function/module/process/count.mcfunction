scoreboard players remove $tool steal_time_count 1
execute store result bossbar ci:steal_time_count value run scoreboard players get $tool steal_time_count
execute if score $tool steal_time_count matches 150 run function steal:module/process/yellow_warn
execute if score $tool steal_time_count matches 75 run function steal:module/process/yellow_warn
bossbar set ci:steal_time_count name [{"text": "已有 ","color": "white"},{"score": {"name": "$tool","objective": "steal_mine"},"color": "red"},{"text": " 块红石矿被拆除","color": "white"}]






#sche