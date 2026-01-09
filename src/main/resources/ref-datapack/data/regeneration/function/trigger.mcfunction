execute as @a unless entity @s[scores={regeneration_sneak_test=1..,team=1..4}] run scoreboard players reset @s regeneration_time
execute as @a[scores={regen_break0=1..}] run function regeneration:stop
execute as @a[scores={regen_break1=1..}] run function regeneration:stop
execute as @a[scores={regen_break2=1..}] run function regeneration:stop
execute as @a[scores={regen_break3=1..}] run function regeneration:stop
execute as @a[scores={regen_break4=1..}] run function regeneration:stop
execute as @a[scores={regen_break5=1..}] run function regeneration:stop
execute as @a[scores={regen_break6=1..}] run function regeneration:stop

execute as @a[scores={regeneration_sneak_test=1..,team=1..4},nbt={Motion:[0.0d,-0.0784000015258789d,0.0d]}] run function regeneration:count
scoreboard players reset @a[scores={regeneration_sneak_test=1..,team=1..4}] regeneration_sneak_test
#sche