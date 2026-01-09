tag @s add job_inventory_checked
execute as @s[scores={job_choose=1}] run function job:1_creeper/inventory/trigger
execute as @s[scores={job_choose=2}] run function job:2_moison/inventory/trigger
execute as @s[scores={job_choose=3}] run function job:3_avenger/inventory/trigger
execute as @s[scores={job_choose=4}] run function job:4_blood_race/inventory/trigger
execute as @s[scores={job_choose=5}] run function job:5_rock/inventory/trigger
execute as @s[scores={job_choose=6}] run function job:6_wolong/inventory/trigger
execute as @s[scores={job_choose=7}] run function job:7_me/inventory/trigger
execute if score $tool mode matches 1 if score $tool game_stage matches 3 as @s[scores={team=2}] run function job:-1_steal/inventory/trigger

execute as @a[limit=1,tag=!job_inventory_checked,tag=ready_for_war] run function job:skill_decide
