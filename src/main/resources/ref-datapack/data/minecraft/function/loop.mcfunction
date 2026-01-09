function minecraft:join/trigger

function murder:trigger




function choosejob:inventory/lead

function lobby:portal/trigger

function lobby:portal/team_pop

function job:inventory_trigger



execute if score $tool mode matches 0 run function minecraft:map/bossbar
execute if score $tool mode matches 0 run function mutation:trigger
function minecraft:map/trigger_change_map

function death:trigger

function death:respawn/time_count

function tools:spawn_point/particle

function murder:reset/count

function regeneration:trigger

function shop:particle/trigger_1t

function reach:particle/lead

function steal:waiting/trigger

function steal:waiting/count_down/trigger_countdown

function steal:start/trigger_start

function death:respawn/steal_spectator/tp

execute if score $tool mode matches 1 if score $tool game_stage matches 3 run function steal:module/end/death/trigger
execute if score $tool mode matches 1 if score $tool game_stage matches 3 run function steal:module/end/time_out/trigger
execute if score $tool mode matches 1 if score $tool game_stage matches 3 run function steal:module/end/mined_enough/trigger
execute if score $tool mode matches 1 if score $tool game_stage matches 3 run function steal:module/end/mined_enough/mine_trigger
 



function job:skill_trigger

schedule function minecraft:loop 1t
#sche