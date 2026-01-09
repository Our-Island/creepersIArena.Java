#等待游戏开始时的bossbar
bossbar set ci:waiting name [{"text":"已准备 ","color":"white"},{"score":{"objective":"ready_number","name":"$tool"},"color":"green"},{"text":" / ","color":"white"},{"text":"需要 ","color":"white"},{"score":{"objective":"need_number","name":"$tool"},"color":"red"}]

execute if score $tool start_countdown matches 0.. run bossbar set ci:waiting name [{"text":"已准备 ","color":"white"},{"score":{"objective":"ready_number","name":"$tool"},"color":"green"},{"text":" / ","color":"white"},{"text":"需要 ","color":"white"},{"score":{"objective":"need_number","name":"$tool"},"color":"green"}]

execute unless score $tool start_countdown matches 0.. store result bossbar ci:waiting max run scoreboard players get $tool population

execute store result bossbar ci:waiting value run scoreboard players get $tool ready_number

bossbar set ci:waiting players @a


#sche-






