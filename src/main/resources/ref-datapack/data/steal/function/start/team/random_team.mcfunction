scoreboard players set $red population 0
scoreboard players set $blue population 0
execute as @a[scores={team=1,steal_ready=1}] run scoreboard players add $red population 1
execute as @a[scores={team=2,steal_ready=1}] run scoreboard players add $blue population 1

execute if score $red population > $blue population run scoreboard players set @s team 2
execute if score $red population < $blue population run scoreboard players set @s team 1
execute if score $red population = $blue population store result score @s team run random value 1..2

execute if score @s team matches 1 run tellraw @s [{"text": "你被分配至","color": "white"},{"text": "红队","color": "red"}]
execute if score @s team matches 2 run tellraw @s [{"text": "你被分配至","color": "white"},{"text": "蓝队","color": "blue"}]