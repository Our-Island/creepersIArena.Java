
scoreboard players set @s murder_type 1003000
scoreboard players set @a[advancements={murder:job/1/3/firework_source=true},limit=1,sort=furthest] murder_time_to_reset 600

scoreboard players operation @s murder_last_source = @a[advancements={murder:job/1/3/firework_source=true},limit=1,sort=furthest] id

advancement revoke @a only murder:job/1/3/hurt_by_firework
advancement revoke @a only murder:job/1/3/firework_source