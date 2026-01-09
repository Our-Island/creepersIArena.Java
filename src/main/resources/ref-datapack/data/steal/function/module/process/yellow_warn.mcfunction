bossbar set ci:steal_time_count color yellow
execute as @a at @s run playsound minecraft:block.note_block.chime player @s ~ ~ ~ 1 2
tellraw @a [{"text": "还剩2分-30秒","color": "yellow"}]