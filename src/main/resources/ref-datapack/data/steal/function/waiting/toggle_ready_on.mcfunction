advancement revoke @s only choosejob:team/ready_trigger

scoreboard players set @s steal_ready 1
team join ready @s
execute at @s run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 2