execute at @s run playsound minecraft:ui.button.click player @s ~ ~ ~ 1 1
#$scoreboard players set @s team_choose $(team_choose)
scoreboard players add @s team_choose 1
execute if score @s team_choose matches 5 run scoreboard players set @s team_choose 0
execute if score $tool mode matches 1 run scoreboard players set @a[scores={team_choose=3..4}] team_choose 0
advancement revoke @s only choosejob:team/1_trigger
advancement revoke @s only choosejob:team/2_trigger
advancement revoke @s only choosejob:team/3_trigger
advancement revoke @s only choosejob:team/4_trigger
advancement revoke @s only choosejob:team/0_trigger