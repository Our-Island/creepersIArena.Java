scoreboard players set @s cd_9 20
scoreboard players set @s cd_9_t 20

scoreboard players set @s[scores={cd_1=..4}] cd_1_t 0
scoreboard players set @s[scores={cd_1=..4}] cd_1 0

scoreboard players set @s[scores={cd_2=..4}] cd_2_t 0
scoreboard players set @s[scores={cd_2=..4}] cd_2 0

scoreboard players set @s[scores={cd_3=..4}] cd_3_t 0
scoreboard players set @s[scores={cd_3=..4}] cd_3 0

scoreboard players remove @s[scores={cd_1=5..}] cd_1 4
scoreboard players remove @s[scores={cd_2=5..}] cd_2 4
scoreboard players remove @s[scores={cd_3=5..}] cd_3 4
execute at @s run playsound minecraft:block.chest.locked player @a[distance=..15] ~ ~ ~ 1 1
execute at @s run particle enchant ~ ~ ~ 0.6 0.3 0.6 0 8 force @a[distance=..30]
execute at @s run particle enchant ~ ~1 ~ 0.6 0.3 0.6 0 8 force @a[distance=..30]
execute at @s run particle enchanted_hit ~ ~ ~ 0.6 0.3 0.6 0 8 force @a[distance=..30]
execute at @s run particle enchanted_hit ~ ~1 ~ 0.6 0.3 0.6 0 8 force @a[distance=..30]