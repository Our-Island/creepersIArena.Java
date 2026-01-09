

execute if score $tool mode matches 0 as @a unless entity @s[x=4985.701,y=67.000,z=4999.000,dx=1.598,dy=2.990,dz=1.598] run scoreboard players reset @s lobby_portal

execute if score $tool mode matches 0 as @a[x=4985.701,y=67.000,z=4999.000,dx=1.598,dy=2.990,dz=1.598] run function lobby:portal/count
#sche