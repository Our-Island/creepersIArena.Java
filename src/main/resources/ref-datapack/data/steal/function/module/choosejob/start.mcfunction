bossbar set ci:choosejob players @a
bossbar set ci:choosejob max 10
bossbar set ci:choosejob value 10
scoreboard players set $tool choosejob_time 10
scoreboard players set $tool game_stage 2
scoreboard players add $tool round 1
effect clear @a

effect give @a[gamemode=adventure] resistance 12 5 true
clear @a
effect give @a instant_health 1 10 true

function steal:start/fill_redstone

execute as @a[scores={team=1..2}] at @s run playsound minecraft:entity.ender_eye.death player @s ~ ~ ~ 1 1
scoreboard players set @a choosejob_page 1
function steal:module/choosejob/barrier
execute as @a[scores={team=1}] run function steal:start/red_tp
execute as @a[scores={team=2}] run function steal:start/blue_tp
