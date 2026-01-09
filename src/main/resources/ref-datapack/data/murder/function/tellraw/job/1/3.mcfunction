
execute store result score $murder_random murder_random run random value 1..3

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 1 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 被 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 的升空烟花炸死了","color": "white"}]

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 2 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 不幸地撞上了 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 的升空烟花","color": "white"}]

$execute if entity @a[tag=murderer] if score $murder_random murder_random matches 3 run tellraw @a [{"text": "$(continued_kill) ➷ ","color": "$(continued_kill_color)"},{"selector":"@s"},{"text": " 在 ","color": "white"},{"selector":"@a[tag=murderer,limit=1]"},{"text":" 的升空烟花的爆炸声中“一岁除”","color": "white"}]


execute unless entity @a[tag=murderer] store result score $murder_random murder_random run random value 1..3

execute unless entity @a[tag=murderer] if score $murder_random murder_random matches 1 run tellraw @a [{"text": "死亡 ➷ ","color": "gray"},{"selector":"@s"},{"text":" 被升空烟花炸死了","color": "white"}]

execute unless entity @a[tag=murderer] if score $murder_random murder_random matches 2 run tellraw @a [{"text": "死亡 ➷ ","color": "gray"},{"selector":"@s"},{"text":" 不幸撞上了升空烟花","color": "white"}]

execute unless entity @a[tag=murderer] if score $murder_random murder_random matches 3 run tellraw @a [{"text": "死亡 ➷ ","color": "gray"},{"selector":"@s"},{"text":" 在升空烟花爆炸声中“一岁除”","color": "white"}]