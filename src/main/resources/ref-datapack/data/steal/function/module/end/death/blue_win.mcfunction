tellraw @a [{"text": ""}]
tellraw @a [{"text": "♕ ","color": "white"},{"text": " 红队全部被击杀","color": "white"}]
tellraw @a [{"text": "♕ ","color": "blue"},{"text": " 本轮蓝队获得了胜利！","color": "blue"}]
scoreboard players add $blue steal_win 1
function steal:module/end/end