scoreboard players add @s lobby_portal 1
execute as @a[scores={lobby_portal=75..}] run function lobby:portal/join_team
execute as @a[scores={lobby_portal=75..}] run function lobby:portal/join
