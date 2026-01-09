
scoreboard players set @s murder_type 2001000
scoreboard players set @a[advancements={murder:job/2/1/arrow_source=true},limit=1,sort=furthest] murder_time_to_reset 600
scoreboard players remove @a[advancements={murder:job/2/1/arrow_source=true},limit=1,sort=furthest] cd_1 2
scoreboard players remove @a[advancements={murder:job/2/1/arrow_source=true},limit=1,sort=furthest] cd_2 2
scoreboard players operation @s murder_last_source = @a[advancements={murder:job/2/1/arrow_source=true},limit=1,sort=furthest] id

advancement revoke @a only murder:job/2/1/hurt_by_arrow
advancement revoke @a only murder:job/2/1/arrow_source