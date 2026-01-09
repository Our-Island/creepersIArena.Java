scoreboard players set $tool red_pop 0
scoreboard players set $tool blue_pop 0
scoreboard players set $tool yellow_pop 0
scoreboard players set $tool green_pop 0
scoreboard players set $tool total_pop 0
execute as @a[scores={team=1}] run scoreboard players add $tool red_pop 1
execute as @a[scores={team=2}] run scoreboard players add $tool blue_pop 1
execute as @a[scores={team=3}] run scoreboard players add $tool yellow_pop 1
execute as @a[scores={team=4}] run scoreboard players add $tool green_pop 1
execute as @a[scores={team=1..4}] run scoreboard players add $tool total_pop 1

#sche
