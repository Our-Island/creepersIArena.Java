bossbar set ci:steal_time_count color red
execute as @a at @s run playsound minecraft:block.note_block.chime player @s ~ ~ ~ 1 0
tellraw @a [{"text": "还剩1分-15秒","color": "red"}]