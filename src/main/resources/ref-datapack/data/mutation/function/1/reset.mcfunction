execute store result score $tool mutation_random run random value 1..4
execute if score $tool mutation_random matches 1 run tellraw @a [{"text": "🕸 ","color":"white"},{"text":"神秘的力量消失了...","color":"gray"}]
execute if score $tool mutation_random matches 2 run tellraw @a [{"text": "🕸 ","color":"white"},{"text":"时间流速恢复正常...","color":"gray"}]
execute if score $tool mutation_random matches 3 run tellraw @a [{"text": "🕸 ","color":"white"},{"text":"你感觉身体变慢了...","color":"gray"}]
execute if score $tool mutation_random matches 4 run tellraw @a [{"text": "🕸 ","color":"white"},{"text":"好像世界变慢了？","color":"gray"}]
execute as @a run attribute @s movement_speed modifier remove mutation:1/speed
tick rate 20
gamerule doDaylightCycle false
time set noon
#permission level is specially set in CreepersImagine properties.yml
execute as @a at @s run playsound block.beacon.deactivate player @s ~ ~ ~ 1 0.8
