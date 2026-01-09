scoreboard players remove @a[scores={murder_time_to_reset=1..}] murder_time_to_reset 1
scoreboard players remove @a[scores={continued_kill_reset_time=1..,continued_kill=1..}] continued_kill_reset_time 1
execute as @a[scores={continued_kill_reset_time=..0}] run function murder:reset/reset
execute as @a[scores={murder_time_to_reset=..0}] run function murder:reset/reset

#sche