#每个mcfunction放10个，tips_random决定这10个中第几个，storage决定第几个mcfunction
execute store result score $tool tips_random run random value 1..10
execute store result storage tips:random index int 1 run random value 1..5
function tips:all_the_tips/trigger with storage tips:random