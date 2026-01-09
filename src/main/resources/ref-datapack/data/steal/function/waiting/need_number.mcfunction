scoreboard players set $tool population 0
execute as @a run scoreboard players add $tool population 1


execute if score $tool population matches 1..2 run scoreboard players set $tool need_number 2
execute if score $tool population matches 3..5 run scoreboard players set $tool need_number 3
execute if score $tool population matches 6..7 run scoreboard players set $tool need_number 4
execute if score $tool population matches 8..9 run scoreboard players set $tool need_number 5
execute if score $tool population matches 10.. run scoreboard players set $tool need_number 6

