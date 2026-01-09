effect give @s slow_falling 1 0 true

#execute as @s at @s run summon firework_rocket ^ ^ ^0.2 {FireworksItem:{id:"firework_rocket",Count:1b,tag:{ Fireworks:{Explosions:[{Colors:[I;8126208],FadeColors:[I;25109],Type:0b}],Flight:1}}},Life:0,LifeTime:6,ShotAtAngle:1b,Motion:[0d,0d,0d],Tags:["job_1_3_new","job_1_3"]}

execute as @s at @s anchored feet run summon firework_rocket ^ ^ ^0.2 {FireworksItem:{components:{fireworks:{explosions:[{fade_colors: [6721280,39445,35668], shape: "creeper", colors: [14221236,9568176,8454097]}]}}, count: 1, id: "minecraft:firework_rocket"},Life:0,LifeTime:16,ShotAtAngle:1b,Motion:[0.0d,0.0d,0.0d],Tags:["job_1_3_new","job_1_3"]}

ride @s mount @e[tag=job_1_3_new,limit=1]

execute at @s as @s anchored eyes run summon marker ^ ^ ^1 {Tags:["job_1_3_marker"]}

execute as @e[tag=job_1_3_marker] store result score @s job_1_3_x run data get entity @s Pos[0] 100
execute as @e[tag=job_1_3_marker] store result score @s job_1_3_z run data get entity @s Pos[2] 100


execute store result score @s job_1_3_x run data get entity @s Pos[0] 100
execute store result score @s job_1_3_z run data get entity @s Pos[2] 100

data modify entity @e[tag=job_1_3_new,limit=1] Motion[1] set value 0.6d

scoreboard players operation @e[tag=job_1_3_marker,limit=1] job_1_3_x -= @s job_1_3_x
execute store result entity @e[tag=job_1_3_new,limit=1] Motion[0] double 0.006 run scoreboard players get @e[tag=job_1_3_marker,limit=1] job_1_3_x


scoreboard players operation @e[tag=job_1_3_marker,limit=1] job_1_3_z -= @s job_1_3_z
execute store result entity @e[tag=job_1_3_new,limit=1] Motion[2] double 0.006 run scoreboard players get @e[tag=job_1_3_marker,limit=1] job_1_3_z

scoreboard players operation @e[tag=job_1_3_new] id = @s id

data merge entity @e[tag=job_1_3_new,limit=1] {Owner:[I;123012012,112203123,112230123,123012123],LeftOwner:1b}

execute store result entity @e[tag=job_1_3_new,limit=1] Owner[0] int 1.0 run data get entity @s UUID[0]
execute store result entity @e[tag=job_1_3_new,limit=1] Owner[1] int 1.0 run data get entity @s UUID[1]
execute store result entity @e[tag=job_1_3_new,limit=1] Owner[2] int 1.0 run data get entity @s UUID[2]
execute store result entity @e[tag=job_1_3_new,limit=1] Owner[3] int 1.0 run data get entity @s UUID[3]

kill @e[tag=job_1_3_marker]
tag @e remove job_1_3_new