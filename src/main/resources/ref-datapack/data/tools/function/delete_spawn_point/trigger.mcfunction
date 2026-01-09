execute store result storage tools:spawn_point map int 1.0 run scoreboard players get @e[tag=map,sort=nearest,limit=1] map_marker
execute if entity @e[distance=..2,tag=spawn_point] run tellraw @s [{"text": "成功删除2格范围内的出生点","color": "yellow"}]
execute unless entity @e[distance=..2,tag=spawn_point] run tellraw @s [{"text": "未能删除新的出生点，","color": "red"}]
tellraw @s [{"text": "地图序号： ","color": "yellow"},{"score":{"name": "@e[tag=map,sort=nearest,limit=1]","objective": "map_marker"}}]

execute if entity @e[distance=..2,tag=spawn_point] as @s at @s run playsound minecraft:entity.arrow.hit_player player @s ~ ~ ~ 1 1
execute unless entity @e[distance=..2,tag=spawn_point] as @s at @s run playsound minecraft:block.note_block.didgeridoo player @s ~ ~ ~ 1 1


kill @e[distance=..2,tag=spawn_point]

scoreboard players set @e[tag=map,sort=nearest,limit=1] spawn_point_number 0
execute as @e[tag=spawn_point,distance=..2000] run scoreboard players add @e[tag=map,sort=nearest,limit=1] spawn_point_number 1
tellraw @s [{"text": "目前此地图共有 ","color": "yellow"},{"score":{"name": "@e[tag=map,sort=nearest,limit=1]","objective": "spawn_point_number"}},{"text": " 个随机出生点"}]
