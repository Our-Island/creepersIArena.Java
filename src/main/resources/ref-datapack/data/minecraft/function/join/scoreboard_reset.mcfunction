scoreboard players reset @s lobby_portal
scoreboard players reset @s death_heart_time
scoreboard players reset @s death_spawn_time
effect clear @s
effect give @s saturation infinite 0 true

scoreboard players reset @s kill_score

scoreboard players reset @s job_2_2_time
scoreboard players reset @s job_2_2_time_t

scoreboard players set @s cd_1 0
scoreboard players set @s cd_2 0
scoreboard players set @s cd_3 0
scoreboard players set @s cd_4 0
scoreboard players set @s cd_5 0
scoreboard players set @s cd_6 0
scoreboard players set @s cd_7 0
scoreboard players set @s cd_8 0
scoreboard players set @s cd_9 0
scoreboard players set @s cd_1_t 0
scoreboard players set @s cd_2_t 0
scoreboard players set @s cd_3_t 0
scoreboard players set @s cd_4_t 0
scoreboard players set @s cd_5_t 0
scoreboard players set @s cd_6_t 0
scoreboard players set @s cd_7_t 0
scoreboard players set @s cd_8_t 0
scoreboard players set @s cd_9_t 0

scoreboard players reset @s job_3_2_time

scoreboard players reset @s job_4_2_time
scoreboard players reset @s job_4_2_x
scoreboard players reset @s job_4_2_time_t
scoreboard players reset @s job_4_2_heart_time
scoreboard players reset @s job_4_2_reset_time
scoreboard players reset @s job_4_3_time
scoreboard players reset @s job_4_4_time

scoreboard players reset @s job_5_2_time
scoreboard players reset @s job_5_3_last_target
scoreboard players reset @s job_5_3_from
execute as @a at @s if score @p job_5_3_last_target = @s id run scoreboard players reset @s job_5_3_last_target

scoreboard players reset @s job_6_1_time
scoreboard players reset @s job_6_3_times

