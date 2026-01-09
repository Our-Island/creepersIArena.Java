
execute if entity @a[tag=murderer] store result score $murder_random murder_random run random value 1..4

$execute if score $murder_random murder_random matches 1 if entity @a[tag=murderer] run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 被 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 一巴掌拍死了","color": "white"}]

$execute if score $murder_random murder_random matches 2 if entity @a[tag=murderer] run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 被 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 一拳打倒","color": "white"}]

$execute if score $murder_random murder_random matches 3 if entity @a[tag=murderer] run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 在与 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 的战斗中被一拳打倒了","color": "white"}]

$execute if score $murder_random murder_random matches 4 if entity @a[tag=murderer] run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 在挨下 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 一拳后再也站不起来了","color": "white"}]

execute unless entity @a[tag=murderer] store result score $murder_random murder_random run random value 1..2

execute if score $murder_random murder_random matches 1 unless entity @a[tag=murderer] run tellraw @a [{"text": "自杀 ➷ ","color": "gray"},{"selector":"@s"},{"text": " 被空手打死了","color": "white"}]

execute if score $murder_random murder_random matches 2 unless entity @a[tag=murderer] run tellraw @a [{"text": "自杀 ➷ ","color": "gray"},{"selector":"@s"},{"text": " 在赤手双拳面前失去了勇气","color": "white"}]
