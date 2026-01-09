
execute if entity @a[tag=murderer] store result score $murder_random murder_random run random value 1..6

$execute if score $murder_random murder_random matches 1 if entity @a[tag=murderer] run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 被 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 驱逐进了虚空","color": "white"}]

$execute if score $murder_random murder_random matches 2 if entity @a[tag=murderer] run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 因为畏惧 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 而踏入虚空","color": "white"}]

$execute if score $murder_random murder_random matches 3 if entity @a[tag=murderer] run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 在与 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 的战斗中跌入了虚空","color": "white"}]

$execute if score $murder_random murder_random matches 4 if entity @a[tag=murderer] run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 在与 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 的战斗中被吓入虚空","color": "white"}]

$execute if score $murder_random murder_random matches 5 if entity @a[tag=murderer] run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 在 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 推荐的 “虚空小屋” 中化成了粉末","color": "white"}]

$execute if score $murder_random murder_random matches 6 if entity @a[tag=murderer] run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 被 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 逼入虚空，迷失了自我","color": "white"}]


execute unless entity @a[tag=murderer] store result score $murder_random murder_random run random value 1..6

execute if score $murder_random murder_random matches 1 unless entity @a[tag=murderer] run tellraw @a [{"text": "自杀 ➷ ","color": "gray"},{"selector":"@s"},{"text": " 迷失在虚空","color": "white"}]

execute if score $murder_random murder_random matches 2 unless entity @a[tag=murderer] run tellraw @a [{"text": "自杀 ➷ ","color": "gray"},{"selector":"@s"},{"text": " 被虚空吞入","color": "white"}]

execute if score $murder_random murder_random matches 3 unless entity @a[tag=murderer] run tellraw @a [{"text": "自杀 ➷ ","color": "gray"},{"selector":"@s"},{"text": " 与虚空融为了一体","color": "white"}]

execute if score $murder_random murder_random matches 4 unless entity @a[tag=murderer] run tellraw @a [{"text": "自杀 ➷ ","color": "gray"},{"selector":"@s"},{"text": " 卷入无尽黑暗的虚空之中","color": "white"}]

execute if score $murder_random murder_random matches 5 unless entity @a[tag=murderer] run tellraw @a [{"text": "自杀 ➷ ","color": "gray"},{"selector":"@s"},{"text": " 在绝望中跳入虚空","color": "white"}]

execute if score $murder_random murder_random matches 6 unless entity @a[tag=murderer] run tellraw @a [{"text": "自杀 ➷ ","color": "gray"},{"selector":"@s"},{"text": " 把自己喂给了虚空","color": "white"}]

