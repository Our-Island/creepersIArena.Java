scoreboard players set @a[advancements={ murder:job/4/1/hit=true}] murder_type 4001000

scoreboard players operation @a[advancements={ murder:job/4/1/hit=true},limit=1] murder_last_source = @s id

scoreboard players set @a[advancements={ murder:job/4/1/hit_source=true},limit=1] murder_time_to_reset 600
effect give @a[advancements={ murder:job/4/1/hit_source=true},limit=1] speed 1 0 false
execute at @a[advancements={ murder:job/4/1/hit=true},limit=1] run playsound particle.soul_escape player @a[distance=..15] ~ ~ ~ 1 2
advancement revoke @a only murder:job/4/1/hit
advancement revoke @a only murder:job/4/1/hit_source

