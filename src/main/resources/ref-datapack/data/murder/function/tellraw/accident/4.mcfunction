
execute if entity @a[tag=murderer] store result score $murder_random murder_random run random value 1..3

$execute if score $murder_random murder_random matches 1 if entity @a[tag=murderer] run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 在与 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 战斗时被迫“燃”起来了","color": "white"}]

$execute if score $murder_random murder_random matches 2 if entity @a[tag=murderer] run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 在与 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 战斗时绝望地死在火里","color": "white"}]

$execute if score $murder_random murder_random matches 3 if entity @a[tag=murderer] run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 在与 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 的战斗中死于高温","color": "white"}]



execute unless entity @a[tag=murderer] store result score $murder_random murder_random run random value 1..4

execute if score $murder_random murder_random matches 1 unless entity @a[tag=murderer] run tellraw @a [{"text": "自杀 ➷ ","color": "gray"},{"selector":"@s"},{"text": " 的洗澡水放得过烫了","color": "white"}]

execute if score $murder_random murder_random matches 2 unless entity @a[tag=murderer] run tellraw @a [{"text": "自杀 ➷ ","color": "gray"},{"selector":"@s"},{"text": " “燃”起来了","color": "white"}]

execute if score $murder_random murder_random matches 3 unless entity @a[tag=murderer] run tellraw @a [{"text": "自杀 ➷ ","color": "gray"},{"selector":"@s"},{"text": " 死于高温","color": "white"}]
execute if score $murder_random murder_random matches 4 unless entity @a[tag=murderer] run tellraw @a [{"text": "自杀 ➷ ","color": "gray"},{"selector":"@s"},{"text": " 试图获得高温补贴，然而死了","color": "white"}]

