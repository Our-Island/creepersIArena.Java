execute store result storage tools:spawn_point map int 1.0 run scoreboard players get @e[tag=map,sort=nearest,limit=1] map_marker
tellraw @s [{"text": "成功添加新的出生点","color": "green"}]
tellraw @s [{"text": "地图序号： ","color": "yellow"},{"score":{"name": "@e[tag=map,sort=nearest,limit=1]","objective": "map_marker"}}]

execute as @s at @s run playsound minecraft:entity.arrow.hit_player player @s ~ ~ ~ 1 1
function tools:spawn_point/summon with storage tools:spawn_point

scoreboard players set @e[tag=map,sort=nearest,limit=1] spawn_point_number 0
execute as @e[tag=spawn_point,distance=..2000] run scoreboard players add @e[tag=map,sort=nearest,limit=1] spawn_point_number 1
tellraw @s [{"text": "此地图共有了 ","color": "yellow"},{"score":{"name": "@e[tag=map,sort=nearest,limit=1]","objective": "spawn_point_number"}},{"text": " 个随机出生点"}]