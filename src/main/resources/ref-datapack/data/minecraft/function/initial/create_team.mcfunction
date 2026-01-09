team remove lobby
team add lobby
team modify lobby color white
team modify lobby friendlyFire false
execute if score $tool mode matches 0 run team join lobby @e[scores={team=0}]

effect give @e[scores={team=0}] resistance 8 5 true

team remove red
team add red
team modify red color red
team modify red friendlyFire false
team join red @e[scores={team=1}]
team modify red collisionRule pushOtherTeams
execute if score $tool mode matches 1 run team modify red nametagVisibility hideForOtherTeams

team remove blue
team add blue
team modify blue color blue
team modify blue friendlyFire false
team join blue @e[scores={team=2}]
team modify blue collisionRule pushOtherTeams
execute if score $tool mode matches 1 run team modify blue nametagVisibility hideForOtherTeams

team remove yellow
team add yellow
team modify yellow color yellow
team modify yellow friendlyFire false
team join yellow @e[scores={team=3}]
team modify yellow collisionRule pushOtherTeams

team remove green
team add green
team modify green color green
team modify green friendlyFire false
team join green @e[scores={team=4}]
team modify green collisionRule pushOtherTeams


team remove ready
team add ready
team modify ready color green
team modify ready friendlyFire false
execute if score $tool mode matches 1 unless score $tool game_stage matches 1.. run team join ready @e[scores={steal_ready=1}]
team modify ready collisionRule pushOwnTeam

team remove unready
team add unready
team modify unready color gray
team modify unready friendlyFire false
execute if score $tool mode matches 1 unless score $tool game_stage matches 1.. run team join unready @e[scores={steal_ready=0}]
team modify unready collisionRule pushOwnTeam

execute if score $tool mode matches 1 unless score $tool game_stage matches 1.. run effect give @a weakness 8 255 true

team remove spectator
team add spectator
team modify spectator color gray


effect give @a saturation infinite 0 true
#sche