
execute store result score $murder_random murder_random run random value 1..5

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 1 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 被 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 的神奇鱼竿钩死了","color": "white"}]

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 2 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 被 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 的神奇鱼钩扇晕了","color": "white"}]

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 3 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 死了，是 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 的神奇鱼竿干的","color": "white"}]

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 4 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 死于 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 的钩子","color": "white"}]

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 5 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 被 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 用神奇鱼竿钩爆了","color": "white"}]