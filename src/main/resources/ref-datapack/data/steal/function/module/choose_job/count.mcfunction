scoreboard players remove $tool choosejob_time 1
execute store result bossbar ci:choosejob value run scoreboard players get $tool choosejob_time
bossbar set ci:choosejob players @a
execute if score $tool choosejob_time matches 0 run function steal:module/choosejob/end
#sche-s