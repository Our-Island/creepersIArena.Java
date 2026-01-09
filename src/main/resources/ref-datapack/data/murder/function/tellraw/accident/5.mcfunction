
execute if entity @a[tag=murderer] store result score $murder_random murder_random run random value 1..3

$execute if score $murder_random murder_random matches 1 if entity @a[tag=murderer] run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 在与 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 战斗时摔死了","color": "white"}]

$execute if score $murder_random murder_random matches 2 if entity @a[tag=murderer] run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 在与 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 战斗过程中摔碎了","color": "white"}]

$execute if score $murder_random murder_random matches 3 if entity @a[tag=murderer] run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 被 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 逼急摔死了","color": "white"}]



execute unless entity @a[tag=murderer] store result score $murder_random murder_random run random value 1..4

execute if score $murder_random murder_random matches 1 unless entity @a[tag=murderer] run tellraw @a [{"text": "自杀 ➷ ","color": "gray"},{"selector":"@s"},{"text": " 摔死了","color": "white"}]

execute if score $murder_random murder_random matches 2 unless entity @a[tag=murderer] run tellraw @a [{"text": "自杀 ➷ ","color": "gray"},{"selector":"@s"},{"text": " 摔碎了","color": "white"}]

execute if score $murder_random murder_random matches 3 unless entity @a[tag=murderer] run tellraw @a [{"text": "自杀 ➷ ","color": "gray"},{"selector":"@s"},{"text": " 以为自己会飞，然而摔死了","color": "white"}]
execute if score $murder_random murder_random matches 4 unless entity @a[tag=murderer] run tellraw @a [{"text": "自杀 ➷ ","color": "gray"},{"selector":"@s"},{"text": " 试图对抗地心引力，然而摔死了","color": "white"}]

