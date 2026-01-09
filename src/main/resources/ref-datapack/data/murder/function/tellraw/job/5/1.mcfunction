
execute store result score $murder_random murder_random run random value 1..5

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 1 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 被 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 的戈仑钻斧劈开了","color": "white"}]

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 2 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 在 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 的戈仑钻斧下低了头","color": "white"}]

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 3 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 没能接下 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 的戈仑钻斧","color": "white"}]

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 4 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 死于 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 手中的戈仑钻斧","color": "white"}]

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 5 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 被 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 用戈仑钻斧送走了","color": "white"}]