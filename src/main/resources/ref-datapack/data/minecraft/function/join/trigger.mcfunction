execute as @a[scores={join_test=1..}] run function minecraft:join/not_new
execute as @a[tag=!not_new] run function minecraft:join/new


#sche