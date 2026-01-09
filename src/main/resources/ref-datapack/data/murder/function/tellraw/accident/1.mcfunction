
execute if entity @a[tag=murderer] store result score $murder_random murder_random run random value 1..2

$execute if score $murder_random murder_random matches 1 if entity @a[tag=murderer] run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 在与 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 的交战中被扎穿了","color": "white"}]

$execute if score $murder_random murder_random matches 2 if entity @a[tag=murderer] run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 在与 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 战斗时被扎的千疮百孔","color": "white"}]



execute unless entity @a[tag=murderer] store result score $murder_random murder_random run random value 1..2

execute if score $murder_random murder_random matches 1 unless entity @a[tag=murderer] run tellraw @a [{"text": "自杀 ➷ ","color": "gray"},{"selector":"@s"},{"text": " 被扎穿了","color": "white"}]

execute if score $murder_random murder_random matches 2 unless entity @a[tag=murderer] run tellraw @a [{"text": "自杀 ➷ ","color": "gray"},{"selector":"@s"},{"text": " 被刺穿了","color": "white"}]