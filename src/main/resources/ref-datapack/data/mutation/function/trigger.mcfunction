# 0 - 无
# 1 - 时间加速
#
#
execute if score $tool mutation_type matches 0 run scoreboard players add $tool mutation_change_time 1
execute if score $tool mutation_type matches 0 if score $tool mutation_change_time matches 6000.. run function mutation:change_mutation


execute if score $tool mutation_type matches 1.. run scoreboard players remove $tool mutation_change_time 1
execute if score $tool mutation_type matches 1.. if score $tool mutation_change_time matches ..0 run function mutation:reset



execute if score $tool mutation_type matches 1 as @a run function mutation:1/add_attribute


#sche-t