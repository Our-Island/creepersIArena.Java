execute at @s run playsound minecraft:ui.button.click player @s ~ ~ ~ 1 1
scoreboard players add @s choosejob_page 1
item replace entity @s hotbar.1 with air
item replace entity @s hotbar.2 with air
item replace entity @s hotbar.3 with air
item replace entity @s hotbar.4 with air
item replace entity @s hotbar.5 with air
item replace entity @s hotbar.6 with air

execute if score @s choosejob_page matches 3 run scoreboard players set @s choosejob_page 1
advancement revoke @s only choosejob:team/toggle_page