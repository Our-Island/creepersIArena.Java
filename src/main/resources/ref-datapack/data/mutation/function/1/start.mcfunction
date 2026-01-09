#决定这个Mutation持续多久
scoreboard players set $tool mutation_change_time 9000
#设置类型
scoreboard players set $tool mutation_type 1
gamerule doDaylightCycle true
execute store result score $tool mutation_random run random value 1..4
execute if score $tool mutation_random matches 1 run tellraw @a [{"text": "🕸 ","color":"white"},{"text":"神秘的力量开始操控魔法","color":"gray"}]
execute if score $tool mutation_random matches 2 run tellraw @a [{"text": "🕸 ","color":"white"},{"text":"一股神秘的力量...","color":"gray"}]
execute if score $tool mutation_random matches 3 run tellraw @a [{"text": "🕸 ","color":"white"},{"text":"时间要开始加速了？还好，只加速了一部分...吗？","color":"gray"}]
execute if score $tool mutation_random matches 4 run tellraw @a [{"text": "🕸 ","color":"white"},{"text":"快快快！太慢了太慢了！","color":"gray"}]

execute store result score $tool mutation_random run random value 1700..2900
execute store result storage ci:mutation/1/time speed float 0.020 run scoreboard players get $tool mutation_random
function mutation:1/change_tick with storage ci:mutation/1/time
execute as @a at @s run playsound block.beacon.activate player @s ~ ~ ~ 1 0.8