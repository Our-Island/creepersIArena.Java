


function job:2_moison/skill/2/lead

function job:3_avenger/skill/2/count

function job:4_blood_race/skill/2/count
function job:4_blood_race/skill/3/count
function job:4_blood_race/skill/4/count

function job:5_rock/skill/2/count
function job:5_rock/skill/3/target_particle

function job:6_wolong/skill/1/count
execute as @e[tag=job_6_2] run function job:6_wolong/skill/2/effect
execute as @a[scores={job_6_3_test=1..,job_choose=6}] run function job:6_wolong/skill/3/trigger
execute as @a[scores={job_6_3_test=1..}] run scoreboard players reset @s job_6_3_test
execute as @a[scores={job_choose=6},tag=ready_for_war] run function job:6_wolong/skill/3/xp
execute as @a[scores={job_choose=6,cd_1=1..,cd_2=1..,cd_3=1..}] unless entity @s[scores={cd_9=1..}] run function job:6_wolong/skill/9/trigger

function job:7_me/skill/2/count
function job:7_me/skill/3/count_trigger

#sche