
scoreboard players set @s murder_type 1002000
scoreboard players set @a[advancements={murder:job/1/2/firework_source=true},limit=1,sort=furthest] murder_time_to_reset 600

scoreboard players operation @s murder_last_source = @a[advancements={murder:job/1/2/firework_source=true},limit=1,sort=furthest] id
effect give @s glowing 1 0 false

advancement revoke @s only murder:job/1/2/hurt_by_firework
advancement revoke @a only murder:job/1/2/firework_source