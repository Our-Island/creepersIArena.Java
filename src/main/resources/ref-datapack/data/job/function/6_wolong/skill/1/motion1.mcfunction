scoreboard players set @s job_6_1_time 18
#in tick
execute positioned as @s run tp @s ~ ~1000 ~
gamemode creative @s
execute positioned as @s at @s anchored eyes positioned ^ ^-0.3 ^1 summon end_crystal run damage @s 1
execute positioned as @s run tp @s ~ ~-1000 ~
gamemode adventure @s
