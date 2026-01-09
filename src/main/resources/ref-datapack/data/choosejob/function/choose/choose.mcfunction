#把@s之前选择的职业改回无附魔
execute if score @s job_choose matches 1 if score @s choosejob_page matches 1 run function choosejob:inventory/1a
execute if score @s job_choose matches 2 if score @s choosejob_page matches 1 run function choosejob:inventory/2a
execute if score @s job_choose matches 3 if score @s choosejob_page matches 1 run function choosejob:inventory/3a
execute if score @s job_choose matches 4 if score @s choosejob_page matches 1 run function choosejob:inventory/4a
execute if score @s job_choose matches 5 if score @s choosejob_page matches 1 run function choosejob:inventory/5a
execute if score @s job_choose matches 6 if score @s choosejob_page matches 2 run function choosejob:inventory/6a
execute if score @s job_choose matches 7 if score @s choosejob_page matches 2 run function choosejob:inventory/7a
#同步此人新的职业选择
$scoreboard players set @s job_choose $(job_index)
execute at @s run playsound minecraft:ui.button.click player @s ~ ~ ~ 1 1
$function choosejob:inventory/$(job_index)b



