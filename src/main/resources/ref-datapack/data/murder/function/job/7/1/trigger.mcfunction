scoreboard players set @a[advancements={ murder:job/7/1/hit=true}] murder_type 7001000

scoreboard players operation @a[advancements={ murder:job/7/1/hit=true},limit=1] murder_last_source = @s id

scoreboard players set @a[advancements={ murder:job/7/1/hit_source=true},limit=1] murder_time_to_reset 600

execute at @a[advancements={ murder:job/7/1/hit=true},limit=1] run playsound minecraft:item.bucket.fill_fish player @a[distance=..15] ~ ~ ~ 1 2
execute as @a[advancements={ murder:job/7/1/hit_source=true},limit=1] run effect give @s glowing 1 0 true
advancement revoke @a only murder:job/7/1/hit
advancement revoke @a only murder:job/7/1/hit_source

