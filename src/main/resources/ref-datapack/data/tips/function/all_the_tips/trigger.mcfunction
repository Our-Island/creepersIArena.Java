$function tips:all_the_tips/$(index)
scoreboard players add @s stats_tips_recieved 1
scoreboard players operation $tool stats_tips_recieved = @s stats_tips_recieved
execute store result storage tips:count temp_operation int 1 run scoreboard players operation $tool stats_tips_recieved %= $20 tool_constant
execute store result storage tips:count number int 1 run scoreboard players get @s stats_tips_recieved
execute if data storage tips:count {temp_operation:0} run function tips:all_the_tips/display with storage tips:count