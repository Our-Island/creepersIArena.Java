scoreboard players reset $tool spectator_time



tellraw @a[scores={team=1..2}] [{"text": ""}]
tellraw @a[scores={team=2}] [{"text": "你作为蓝队一员，合作拆除10块红石矿即可获得胜利","color": "gray"}]
tellraw @a[scores={team=2}] [{"text": "如果3分钟内没有拆除足够红石矿则失败","color": "gray"}]

tellraw @a[scores={team=1}] [{"text": "你作为红队一员，合作守护红石矿即可获得胜利","color": "gray"}]
tellraw @a[scores={team=1}] [{"text": "如果3分钟内被拆除了10块红石矿则失败","color": "gray"}]

bossbar set ci:spectator players


function steal:module/choosejob/start