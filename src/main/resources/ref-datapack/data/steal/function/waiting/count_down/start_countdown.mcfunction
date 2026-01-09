scoreboard players set $tool start_countdown 15
bossbar set ci:waiting color green
execute as @a at @a run playsound minecraft:entity.player.levelup player @s ~ ~ ~ 1 1
title @a[scores={steal_ready=1}] actionbar [{"text": "✪ ","color": "green"},{"text": "游戏将在15秒内开始，你将参与游戏","color": "green"}]
title @a[scores={steal_ready=0}] actionbar [{"text": "✪ ","color": "red"},{"text": "游戏将在15秒内开始，","color": "red"},{"text": "你还未准备","color": "red","underlined": true}]

