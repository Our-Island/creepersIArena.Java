tellraw @a [{"text": ""}]
tellraw @a [{"text": "♕ ","color": "white"},{"text": " 蓝队拆除了足够的红石矿","color": "white"}]
tellraw @a [{"text": "♕ ","color": "blue"},{"text": " 本轮蓝队获得了胜利","color": "blue"}]
scoreboard players add $blue steal_win 1
function steal:module/end/end