#execute as @a[scores={particle_choose=4}] at @s run particle minecraft:entity_effect{color:[1.0,1.0,1.0,0.5]} ~ ~ ~ 0.1 0 0.1 0.4 15 force @a[distance=..15]

$execute at @s run particle minecraft:entity_effect{color:[$(arg1),$(arg2),$(arg3),$(arg4)]} ~ ~ ~ 0.1 0 0.1 0.4 3 force @a[distance=..15]