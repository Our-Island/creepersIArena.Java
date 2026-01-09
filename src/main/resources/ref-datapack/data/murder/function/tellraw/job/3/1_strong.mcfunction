
execute store result score $murder_random murder_random run random value 1..5

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 1 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 被 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 的复仇之刃斩下首级","color": "white"}]

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 2 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 被 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 的复仇之刃劈成了两半","color": "white"}]

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 3 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 在被 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 的复仇之刃砍中后不甘地逝去","color": "white"}]

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 4 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 在与 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 的复仇之刃交手中无奈倒下","color": "white"}]

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 5 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 被 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 用复仇之刃击穿","color": "white"}]