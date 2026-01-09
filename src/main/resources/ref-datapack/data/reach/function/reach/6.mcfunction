execute as @a at @s run playsound minecraft:block.bell.use player @s ~ ~ ~ 1 2
execute at @s run playsound minecraft:entity.player.levelup player @s ~ ~ ~ 1 0
scoreboard players add @s wealth_gunpowder 5
scoreboard players add @s wealth_tnt 1