scoreboard players reset @s job_6_1_time
execute at @s run playsound minecraft:entity.breeze.wind_burst player @a[distance=..15] ~ ~ ~ 1 0
execute positioned as @s run tp @s ~ ~1000 ~
gamemode creative @s
execute positioned as @s at @s anchored eyes positioned ^ ^-0.3 ^-0.6 summon end_crystal run damage @s 1
execute positioned as @s run tp @s ~ ~-1000 ~
gamemode adventure @s
