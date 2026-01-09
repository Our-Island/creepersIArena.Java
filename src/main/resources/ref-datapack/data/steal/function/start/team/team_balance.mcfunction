scoreboard players set $red population 0
scoreboard players set $blue population 0
execute as @a[scores={team=1,steal_ready=1}] run scoreboard players add $red population 1
execute as @a[scores={team=2,steal_ready=1}] run scoreboard players add $blue population 1

#计算人数差
execute if score $red population > $blue population run scoreboard players operation $tool population = $red population
execute if score $red population > $blue population run scoreboard players operation $tool population -= $blue population

execute if score $red population < $blue population run scoreboard players operation $tool population = $blue population
execute if score $red population < $blue population run scoreboard players operation $tool population -= $red population

execute if score $tool population matches 2.. if score @s team matches 1 if score $red population > $blue population run scoreboard players set @s team 2

execute if score $tool population matches 2.. if score @s team matches 2 if score $red population < $blue population run scoreboard players set @s team 1


execute if score @s team = @s team run tellraw @s[scores={team=1}] [{"text": "你加入了","color": "white"},{"text": "红队","color": "red"}]
execute if score @s team = @s team run tellraw @s[scores={team=2}] [{"text": "你加入了","color": "white"},{"text": "蓝队","color": "blue"}]

execute unless score @s team = @s team run tellraw @s[scores={team=1}] [{"text": "你因为队伍人数不平衡，被分入了","color": "white"},{"text": "红队","color": "red"}]
execute unless score @s team = @s team run tellraw @s[scores={team=2}] [{"text": "你因为队伍人数不平衡，被分入了","color": "white"},{"text": "红队","color": "red"}]