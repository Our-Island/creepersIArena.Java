scoreboard players operation $tool steal_mine += @s steal_mine
scoreboard players reset @s steal_mine
tellraw @a [{"text": "☸ ","color": "blue"},{ "selector": "@s"},{"text": " 拆除了一块红石矿 ( ","color": "blue"},{"score":{"name": "$tool","objective": "steal_mine"},"color": "blue"},{"text": " / 10 )","color": "blue"}]
scoreboard players set @s cd_7 3
scoreboard players set @s cd_7_t 20
effect give @s glowing 5 0 true
execute as @a at @s run playsound minecraft:item.totem.use player @s ~ ~ ~ 0.2 0
bossbar set ci:steal_time_count name [{"text": "已有 ","color": "white"},{"score": {"name": "$tool","objective": "steal_mine"},"color": "red"},{"text": " 块红石矿被拆除","color": "white"}]

function regeneration:stop