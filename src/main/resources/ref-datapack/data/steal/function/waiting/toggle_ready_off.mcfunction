advancement revoke @s only choosejob:team/unready_trigger

scoreboard players set @s steal_ready 0
team join unready @s

execute at @s run playsound minecraft:block.note_block.xylophone player @s ~ ~ ~ 1 0