tellraw @a [{"text": ""}]
tellraw @a [{"text": "♕ ","color": "white"},{"text": " 蓝队全部被击杀","color": "white"}]
tellraw @a [{"text": "♕ ","color": "red"},{"text": " 本轮红队获得了胜利！","color": "red"}]
scoreboard players add $red steal_win 1
function steal:module/end/end