execute at @s as @a if score @s id = @p job_5_3_from run tag @s add job_5_3_fangs_owner
execute at @s run summon evoker_fangs ~ ~ ~ {Warmup:0,Tags:["job_5_3_fang","job_5_3_fang_new"]}
effect give @s slowness 2 255 true
effect give @s jump_boost 2 255 true
execute at @s run particle enchant ~ ~1 ~ 0.8 1.2 0.8 0.1 35 force @a[distance=..30]
execute at @s run playsound minecraft:entity.evoker.prepare_summon player @a[distance=..15] ~ ~ ~ 1 0
data modify entity @e[tag=job_5_3_fang_new,limit=1] Owner set from entity @a[tag=job_5_3_from,limit=1] UUID
scoreboard players operation @e[tag=job_5_3_fang_new] id = @a[tag=job_5_3_fangs_owner] id
effect clear @s levitation
effect give @s levitation 1 0 true

tag @e remove job_5_3_fang_new
scoreboard players reset @s job_5_3_from
tag @a remove job_5_3_fangs_owner