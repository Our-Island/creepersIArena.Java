tp @a -20000 110 -20000 180 90
tellraw @a [{"text": "此处是地图中心，共10块红石矿石","color": "gray"}]
execute as @a at @s run playsound minecraft:block.enchantment_table.use player @s ~ ~ ~ 1 0