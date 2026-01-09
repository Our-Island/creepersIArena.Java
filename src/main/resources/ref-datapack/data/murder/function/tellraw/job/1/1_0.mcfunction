
execute store result score $murder_random murder_random run random value 1..3

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 1 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 在 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 的苦力怕召唤面前闭上了嘴巴","color": "white"}]

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 2 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 被 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 的苦力怕送到了梦中","color": "white"}]

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 3 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 在 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 的苦力怕的灰烬中迷失了自我","color": "white"}]


execute unless entity @a[tag=murderer] store result score $murder_random murder_random run random value 1..3

execute unless entity @a[tag=murderer] if score $murder_random murder_random matches 1 run tellraw @a [{"text": "死亡 ➷ ","color": "gray"},{"selector":"@s"},{"text":" 在苦力怕召唤面前闭上了嘴巴","color": "white"}]

execute unless entity @a[tag=murderer] if score $murder_random murder_random matches 2 run tellraw @a [{"text": "死亡 ➷ ","color": "gray"},{"selector":"@s"},{"text":" 被苦力怕送到了梦中","color": "white"}]

execute unless entity @a[tag=murderer] if score $murder_random murder_random matches 3 run tellraw @a [{"text": "死亡 ➷ ","color": "gray"},{"selector":"@s"},{"text":" 在苦力怕的灰烬中迷失了自我","color": "white"}]