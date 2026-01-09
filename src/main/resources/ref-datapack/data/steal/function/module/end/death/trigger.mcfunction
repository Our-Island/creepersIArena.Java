scoreboard players set $red population 0
scoreboard players set $blue population 0
execute as @a[scores={team=1,steal_ready=1},tag=ready_for_war] run scoreboard players add $red population 1
execute as @a[scores={team=2,steal_ready=1},tag=ready_for_war] run scoreboard players add $blue population 1
execute if score $red population matches 0 if score $blue population matches 0 run function steal:module/end/death/draw
execute if score $blue population matches 0 unless score $red population matches 0 run function steal:module/end/death/red_win
execute if score $red population matches 0 unless score $blue population matches 0 run function steal:module/end/death/blue_win
#sche