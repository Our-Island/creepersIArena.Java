execute as @s at @s anchored eyes run summon armor_stand ~ ~ ~ {Tags:["job_7_3_new","job_7_3"],Invisible:1b,equipment:{head:{id:player_head,count:1,components:{profile:{id:[I;2080793942,-524468218,-1541115779,1949756395],properties:[{name:"textures",value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzNiMTk1NWQzYjZlYjQyZjUwZTUzNmMxYTMyODVhYjczZWQ3ZTJiZTA1MWIwOWIyMWUxNzgxMWYxYTZkIn19fQ=="}]}}}},Small:1b,Invulnerable:1b}

execute store result storage ci:job/7/3 direction int 1 run random value -180..180
execute store result storage ci:job/7/3 distance double 0.001 run random value 5..10

tag @s add job_7_3_owner
execute as @e[tag=job_7_3_new] run function job:7_me/skill/3/pufferfish_throw with storage ci:job/7/3
