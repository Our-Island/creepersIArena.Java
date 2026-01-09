gamemode spectator @s
tag @s remove ready_for_war
execute if score @s team matches 1 run tp @s @a[gamemode=adventure,scores={team=1},sort=nearest,limit=1]
execute if score @s team matches 2 run tp @s @a[gamemode=adventure,scores={team=2},sort=nearest,limit=1]

execute if score @s team matches 1 unless entity @a[gamemode=adventure,scores={team=1},sort=nearest,limit=1] run tp @s -20000 100 -20000
execute if score @s team matches 2 unless entity @a[gamemode=adventure,scores={team=2},sort=nearest,limit=1] run tp @s -20000 100 -20000