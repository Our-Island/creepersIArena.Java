
execute store result score $murder_random murder_random run random value 1..3

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 1 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 与 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 战斗时被自己的苦力怕炸死了","color": "white"}]

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 2 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 与 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 交战之时，撞上了自己的苦力怕","color": "white"}]

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 3 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 在被 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 击杀前先被自己炸死了","color": "white"}]


execute unless entity @a[tag=murderer] store result score $murder_random murder_random run random value 1..3

execute unless entity @a[tag=murderer] if score $murder_random murder_random matches 1 run tellraw @a [{"text": "自杀 ➷ ","color": "gray"},{"selector":"@s"},{"text":" 被自己的苦力怕炸死了","color": "white"}]

execute unless entity @a[tag=murderer] if score $murder_random murder_random matches 2 run tellraw @a [{"text": "自杀 ➷ ","color": "gray"},{"selector":"@s"},{"text":" 在用苦力怕炸死自己方面很有天赋","color": "white"}]

execute unless entity @a[tag=murderer] if score $murder_random murder_random matches 3 run tellraw @a [{"text": "自杀 ➷ ","color": "gray"},{"selector":"@s"},{"text":" 在被击杀前先被自己炸死了","color": "white"}]