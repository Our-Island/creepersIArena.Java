
execute store result score $murder_random murder_random run random value 1..3

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 1 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 被 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 用河豚炸死了","color": "white"}]

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 2 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 被 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 的河豚炸穿了","color": "white"}]

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 3 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 在踩到 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 丢在地上的河豚后死了","color": "white"}]


execute unless entity @a[tag=murderer] store result score $murder_random murder_random run random value 1..3

execute unless entity @a[tag=murderer] if score $murder_random murder_random matches 1 run tellraw @a [{"text": "死亡 ➷ ","color": "gray"},{"selector":"@s"},{"text":" 被河豚炸死了","color": "white"}]

execute unless entity @a[tag=murderer] if score $murder_random murder_random matches 2 run tellraw @a [{"text": "死亡 ➷ ","color": "gray"},{"selector":"@s"},{"text":" 被河豚炸穿了","color": "white"}]

execute unless entity @a[tag=murderer] if score $murder_random murder_random matches 3 run tellraw @a [{"text": "死亡 ➷ ","color": "gray"},{"selector":"@s"},{"text":" 在踩到地上的河豚后死了","color": "white"}]