scoreboard players reset $tool start_countdown
bossbar set ci:waiting color red
title @a actionbar [{"text": "✪ ","color": "red"},{"text": "已准备人数不足，需要更多玩家准备","color": "red"}]
execute as @a at @a run playsound minecraft:block.note_block.didgeridoo player @s ~ ~ ~ 1 1
