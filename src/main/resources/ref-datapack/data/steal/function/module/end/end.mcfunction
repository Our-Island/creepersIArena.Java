effect give @a[gamemode=adventure] resistance 12 5 true
bossbar set ci:steal_time_count players
bossbar set ci:celebration players @a

function job:0_common/entity_reset
tellraw @a [{"text": "♨ ","color": "white"},{"text": " 当前比分 ","color": "white"},{"score":{"name": "$red","objective": "steal_win"},"color": "red"},{"text": " : ","color": "white"},{"score":{"name": "$blue","objective": "steal_win"},"color": "blue"}]

execute if score $blue steal_win matches 4.. run function steal:module/end/all_end/trigger
execute if score $red steal_win matches 4.. run function steal:module/end/all_end/trigger

execute unless score $red steal_win matches 4.. unless score $blue steal_win matches 4.. run function steal:module/end/normal_celebrate/trigger

execute if score $blue steal_win matches 4.. run title @a title [{"text": "蓝队获得了胜利！","color": "blue","bold": true}]
execute if score $red steal_win matches 4.. run title @a title [{"text": "红队获得了胜利！","color": "red","bold": true}]