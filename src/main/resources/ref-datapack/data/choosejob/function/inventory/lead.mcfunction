execute if score $tool mode matches 0 as @a[scores={choosejob_page=1},tag=!ready_for_war] run function choosejob:inventory/lead_page_1
execute if score $tool mode matches 0 as @a[scores={choosejob_page=2},tag=!ready_for_war] run function choosejob:inventory/lead_page_2
execute as @a[tag=!ready_for_war] if score $tool mode matches 0 run function choosejob:inventory/lead_page_0
execute as @a[tag=!ready_for_war] if score $tool mode matches 1 if score $tool game_stage matches 2 run function choosejob:inventory/lead_page_toggle_page
execute as @a[tag=!ready_for_war] if score $tool mode matches 0 run function choosejob:inventory/lead_page_toggle_page

execute as @a[tag=!ready_for_war] if score $tool mode matches 1 unless score $tool game_stage matches 1.. run function choosejob:inventory/lead_page_0_steal
execute if score $tool mode matches 1 unless score $tool game_stage matches 1.. as @a run function choosejob:inventory/lead_page_-1

execute if score $tool mode matches 1 if score $tool game_stage matches 2 as @a[scores={choosejob_page=1},tag=!ready_for_war] run function choosejob:inventory/lead_page_1
execute if score $tool mode matches 1 if score $tool game_stage matches 2 as @a[scores={choosejob_page=2},tag=!ready_for_war] run function choosejob:inventory/lead_page_2
#sche