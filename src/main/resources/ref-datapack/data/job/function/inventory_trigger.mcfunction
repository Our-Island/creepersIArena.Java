tag @a remove job_inventory_checked
tag @a remove cd_checked

scoreboard players remove @a[scores={cd_1=1..}] cd_1_t 1
scoreboard players remove @a[scores={cd_2=1..}] cd_2_t 1
scoreboard players remove @a[scores={cd_3=1..}] cd_3_t 1
scoreboard players remove @a[scores={cd_4=1..}] cd_4_t 1
scoreboard players remove @a[scores={cd_5=1..}] cd_5_t 1
scoreboard players remove @a[scores={cd_6=1..}] cd_6_t 1
scoreboard players remove @a[scores={cd_7=1..}] cd_7_t 1
scoreboard players remove @a[scores={cd_8=1..}] cd_8_t 1
scoreboard players remove @a[scores={cd_9=1..}] cd_9_t 1

execute as @a[scores={cd_1_t=..0,cd_1=1..}] run scoreboard players remove @s cd_1 1
execute as @a[scores={cd_1_t=..0,cd_1=1..}] run scoreboard players set @s cd_1_t 20
execute as @a[scores={cd_2_t=..0,cd_2=1..}] run scoreboard players remove @s cd_2 1
execute as @a[scores={cd_2_t=..0,cd_2=1..}] run scoreboard players set @s cd_2_t 20
execute as @a[scores={cd_3_t=..0,cd_3=1..}] run scoreboard players remove @s cd_3 1
execute as @a[scores={cd_3_t=..0,cd_3=1..}] run scoreboard players set @s cd_3_t 20
execute as @a[scores={cd_4_t=..0,cd_4=1..}] run scoreboard players remove @s cd_4 1
execute as @a[scores={cd_4_t=..0,cd_4=1..}] run scoreboard players set @s cd_4_t 20
execute as @a[scores={cd_5_t=..0,cd_5=1..}] run scoreboard players remove @s cd_5 1
execute as @a[scores={cd_5_t=..0,cd_5=1..}] run scoreboard players set @s cd_5_t 20
execute as @a[scores={cd_6_t=..0,cd_6=1..}] run scoreboard players remove @s cd_6 1
execute as @a[scores={cd_6_t=..0,cd_6=1..}] run scoreboard players set @s cd_6_t 20
execute as @a[scores={cd_7_t=..0,cd_7=1..}] run scoreboard players remove @s cd_7 1
execute as @a[scores={cd_7_t=..0,cd_7=1..}] run scoreboard players set @s cd_7_t 20
execute as @a[scores={cd_8_t=..0,cd_8=1..}] run scoreboard players remove @s cd_8 1
execute as @a[scores={cd_8_t=..0,cd_8=1..}] run scoreboard players set @s cd_8_t 20
execute as @a[scores={cd_9_t=..0,cd_9=1..}] run scoreboard players remove @s cd_9 1
execute as @a[scores={cd_9_t=..0,cd_9=1..}] run scoreboard players set @s cd_9_t 20

execute as @a[limit=1,tag=!job_inventory_checked,tag=ready_for_war] run function job:skill_decide
execute as @a[limit=1,tag=!cd_checked,tag=ready_for_war] run function job:cd_decide
#sche