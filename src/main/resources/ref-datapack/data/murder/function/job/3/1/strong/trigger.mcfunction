scoreboard players set @a[advancements={ murder:job/3/1/strong_hit=true}] murder_type 3001200

scoreboard players operation @a[advancements={ murder:job/3/1/strong_hit=true},limit=1] murder_last_source = @s id

scoreboard players set @a[advancements={ murder:job/3/1/strong_hit_source=true},limit=1] murder_time_to_reset 600
execute at @a[advancements={ murder:job/3/1/normal_hit=true},limit=1] run playsound minecraft:item.axe.strip player @a[distance=..15] ~ ~ ~ 1 0
advancement revoke @a only murder:job/3/1/strong_hit
advancement revoke @a only murder:job/3/1/strong_hit_source

