execute if score $too mode matches 1 if score $tool game_stage matches 3 as @a[scores={team=1},gamemode=spectator,distance=..3] at @s unless entity @a[scores={team=1},gamemode=adventure] run tp @s @a[scores={team=1},sort=nearest,limit=1]
execute if score $too mode matches 1 if score $tool game_stage matches 3 as @a[scores={team=2},gamemode=spectator,distance=..3] at @s unless entity @a[scores={team=2},gamemode=adventure] run tp @s @a[scores={team=2},sort=nearest,limit=1]

#sche