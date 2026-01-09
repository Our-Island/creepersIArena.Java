scoreboard players set @a[advancements={ murder:job/3/1/normal_hit=true}] murder_type 3001100

scoreboard players operation @a[advancements={ murder:job/3/1/normal_hit=true},limit=1] murder_last_source = @s id

scoreboard players set @a[advancements={ murder:job/3/1/normal_hit_source=true},limit=1] murder_time_to_reset 600
execute at @a[advancements={ murder:job/3/1/normal_hit=true},limit=1] run playsound minecraft:block.sand.break player @a[distance=..15] ~ ~ ~ 1 1.5
advancement revoke @a only murder:job/3/1/normal_hit
advancement revoke @a only murder:job/3/1/normal_hit_source

