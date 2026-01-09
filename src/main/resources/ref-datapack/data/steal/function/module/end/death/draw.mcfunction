tellraw @a [{"text": ""}]
tellraw @a [{"text": "♕ ","color": "white"},{"text": " 双方同时全部死亡！由于蓝队没有拆除足够的红石矿......","color": "white"}]
tellraw @a [{"text": "♕ ","color": "red"},{"text": " 本轮红队获得了胜利！","color": "red"}]
scoreboard players add $red steal_win 1
function steal:module/end/end