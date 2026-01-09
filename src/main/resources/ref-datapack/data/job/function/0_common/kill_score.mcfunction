scoreboard objectives add kill_score_new dummy
execute as @a run scoreboard players operation @s kill_score_new = @s kill_score
scoreboard objectives remove kill_score
scoreboard objectives add kill_score dummy {"text": "击杀数", "color": "yellow"}
scoreboard objectives setdisplay sidebar kill_score
execute as @a run scoreboard players operation @s kill_score = @s kill_score_new

#sche-
