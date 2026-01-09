scoreboard objectives add debug dummy

scoreboard objectives add mode dummy
execute unless score $tool mode matches 0.. run scoreboard players set $tool mode 0
#mode-0乱斗,mode-1偷窃
scoreboard objectives add steal_ready dummy
scoreboard objectives add game_stage dummy
execute unless score $tool game_stage matches 0.. run scoreboard players reset $tool game_stage
#0准备,1选职业,2战斗中,3结算
scoreboard objectives add population dummy
scoreboard objectives add need_number dummy
scoreboard objectives add ready_number dummy
scoreboard objectives add start_countdown dummy

scoreboard objectives add spectator_time dummy

scoreboard objectives add choosejob_time dummy

scoreboard objectives add round dummy

scoreboard objectives add steal_time_count dummy

scoreboard objectives add steal_mine minecraft.mined:deepslate_redstone_ore

scoreboard objectives add steal_win dummy

scoreboard objectives add celebrate_time_count dummy
#共7 rounds

#constant
scoreboard objectives add tool_constant dummy
scoreboard players set $5 tool_constant 5
scoreboard players set $10 tool_constant 10
scoreboard players set $20 tool_constant 20

#tips随机数
scoreboard objectives add tips_random dummy

#抄的block_arena，感谢龙猫
scoreboard objectives add regen_break0 minecraft.custom:minecraft.damage_taken "打断静息治疗0"
scoreboard objectives add regen_break1 minecraft.custom:minecraft.damage_dealt "打断静息治疗1"
scoreboard objectives add regen_break2 minecraft.custom:minecraft.walk_one_cm "打断静息治疗2"
scoreboard objectives add regen_break3 minecraft.custom:minecraft.sprint_one_cm "打断静息治疗3"
scoreboard objectives add regen_break4 minecraft.custom:minecraft.crouch_one_cm "打断静息治疗4"
scoreboard objectives add regen_break5 minecraft.custom:minecraft.walk_on_water_one_cm "打断静息治疗5"
scoreboard objectives add regen_break6 minecraft.custom:minecraft.jump "打断静息治疗6"

scoreboard objectives add HP health
scoreboard objectives setdisplay below_name HP
scoreboard objectives setdisplay list HP

scoreboard objectives add particle_choose dummy

scoreboard objectives add regeneration_time dummy
scoreboard objectives add regeneration_sneak_test minecraft.custom:sneak_time

scoreboard objectives add death_trigger deathCount
scoreboard objectives add death_spawn_time dummy
scoreboard objectives add death_heart_time dummy

scoreboard objectives add team_choose dummy

scoreboard objectives add change_map_time dummy
scoreboard objectives add map_pos dummy
scoreboard objectives add map_now dummy
execute unless score $tool map_now matches 1.. run scoreboard players set $tool map_now 1

scoreboard objectives add wealth_gunpowder dummy
scoreboard objectives add wealth_tnt dummy

scoreboard objectives add join_test custom:leave_game

scoreboard objectives add id dummy
scoreboard objectives add sub_id dummy
scoreboard objectives add murder_type dummy
# [职业序号][][][技能序号][序号][其他-0，队友-1，自己-2][小分支] 7位
scoreboard objectives add murder_last_source dummy
scoreboard objectives add murder_random dummy
scoreboard objectives add murder_time_to_reset dummy
scoreboard objectives add continued_kill dummy
scoreboard objectives add continued_kill_reset_time dummy
scoreboard objectives add murder_void_y dummy

#统计
scoreboard objectives add stats_kill dummy
scoreboard objectives add stats_death dummy

#统计-职业击杀与死亡-还没写
scoreboard objectives add stats_job_1_kill dummy
scoreboard objectives add stats_job_1_death dummy

scoreboard objectives add stats_job_1_kill dummy
scoreboard objectives add stats_job_1_death dummy

scoreboard objectives add stats_job_1_kill dummy
scoreboard objectives add stats_job_1_death dummy

scoreboard objectives add stats_job_1_kill dummy
scoreboard objectives add stats_job_1_death dummy

scoreboard objectives add stats_job_1_kill dummy
scoreboard objectives add stats_job_1_death dummy

#统计-接受了多少次小提示
scoreboard objectives add stats_tips_recieved dummy


##
scoreboard objectives add team dummy
#0-lobby 1-red 2-blue 3-yellow 4-green,-1-spectator

scoreboard objectives add job_choose dummy
scoreboard objectives add choosejob_page dummy

scoreboard objectives add lobby_portal dummy

scoreboard objectives add map_marker dummy
scoreboard objectives add spawn_point dummy
scoreboard objectives add spawn_point_number dummy

scoreboard objectives add red_pop dummy
scoreboard objectives add blue_pop dummy
scoreboard objectives add yellow_pop dummy
scoreboard objectives add green_pop dummy
scoreboard objectives add least_pop dummy
scoreboard objectives add total_pop dummy

scoreboard objectives add cd_1 dummy
scoreboard objectives add cd_2 dummy
scoreboard objectives add cd_3 dummy
scoreboard objectives add cd_4 dummy
scoreboard objectives add cd_5 dummy
scoreboard objectives add cd_6 dummy
scoreboard objectives add cd_7 dummy
scoreboard objectives add cd_8 dummy
scoreboard objectives add cd_9 dummy

scoreboard objectives add cd_1_t dummy
scoreboard objectives add cd_2_t dummy
scoreboard objectives add cd_3_t dummy
scoreboard objectives add cd_4_t dummy
scoreboard objectives add cd_5_t dummy
scoreboard objectives add cd_6_t dummy
scoreboard objectives add cd_7_t dummy
scoreboard objectives add cd_8_t dummy
scoreboard objectives add cd_9_t dummy


scoreboard objectives add job_1_1_x dummy
scoreboard objectives add job_1_1_y dummy
scoreboard objectives add job_1_1_z dummy


scoreboard objectives add job_1_3_x dummy
scoreboard objectives add job_1_3_y dummy
scoreboard objectives add job_1_3_z dummy


scoreboard objectives add job_2_1_x dummy
scoreboard objectives add job_2_1_y dummy
scoreboard objectives add job_2_1_z dummy

scoreboard objectives add job_2_2_life dummy

scoreboard objectives add job_2_2_x dummy
scoreboard objectives add job_2_2_y dummy
scoreboard objectives add job_2_2_z dummy

scoreboard objectives add job_2_2_type dummy
scoreboard objectives add job_2_2_time_t dummy
scoreboard objectives add job_2_2_time dummy

scoreboard objectives add job_3_2_time dummy

scoreboard objectives add job_4_2_time dummy
scoreboard objectives add job_4_2_x dummy
scoreboard objectives add job_4_2_time_t dummy
scoreboard objectives add job_4_2_marker_life dummy
scoreboard objectives add job_4_2_heart_time dummy
scoreboard objectives add job_4_2_success dummy
scoreboard objectives add job_4_2_reset_time dummy
scoreboard objectives add job_4_3_time dummy
scoreboard objectives add job_4_4_time dummy

scoreboard objectives add job_5_2_time dummy
scoreboard objectives add job_5_3_last_target dummy
scoreboard objectives add job_5_3_from dummy

scoreboard objectives add job_6_1_time dummy
scoreboard objectives add job_6_2_life dummy
scoreboard objectives add job_6_3_times dummy
scoreboard objectives add job_6_3_test minecraft.used:minecraft.crossbow

scoreboard objectives add job_7_2_time dummy
scoreboard objectives add job_7_3_time dummy
scoreboard objectives add job_7_3_x dummy
scoreboard objectives add job_7_3_y dummy
scoreboard objectives add job_7_3_z dummy
scoreboard objectives add job_7_3_pufferfish_time dummy
scoreboard objectives add job_7_3_pufferfish_time_temp dummy
scoreboard objectives add job_7_3_distance dummy


scoreboard objectives add kill_score dummy {"text": "击杀数", "color": "yellow"}
scoreboard objectives setdisplay sidebar kill_score
scoreboard objectives add team_score dummy

#突变的变量:
scoreboard objectives add mutation_change_time dummy
scoreboard objectives add mutation_type dummy
scoreboard objectives add mutation_random dummy