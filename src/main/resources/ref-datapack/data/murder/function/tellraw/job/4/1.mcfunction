
execute store result score $murder_random murder_random run random value 1..5

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 1 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 被 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 的血镰收割了","color": "white"}]

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 2 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 在与 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 战斗时被血镰吸干了","color": "white"}]

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 3 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 的首级被 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 用血镰装进了袋子","color": "white"}]

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 4 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 死于 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 手中的血镰","color": "white"}]

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 5 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 被 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 用血镰击杀","color": "white"}]