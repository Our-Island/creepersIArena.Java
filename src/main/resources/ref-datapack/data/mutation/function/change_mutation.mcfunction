execute if entity @a store result score $tool mutation_random run random value -5..10
execute unless entity @a store result score $tool mutation_random run random value -5..0
execute if score $tool mutation_random matches ..0 run scoreboard players set $tool mutation_change_time 500
execute if score $tool mutation_random matches 1..10 run function mutation:1/start