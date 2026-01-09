advancement revoke @s only job:4_blood_race/2_trigger

execute at @s run playsound minecraft:block.enchantment_table.use player @a[distance=..15] ~ ~ ~ 1 2

#item replace entity @s hotbar.1 with magma_cream{HideFlags:255,display:{Name:[{"text": "嗜血宝珠","color": "white","italic": false}],Lore:[[{"text": "✎ 持续发射激光，命中次数越多，获得的临时血量越多","color": "gray","italic": false}],[{"text": "❃ ","color": "gray","italic": false},{"text": "右键","color": "white","italic": false},{"text": "使用 │ 冷却","color": "gray","italic": false},{"text": " 13 ","color": "white","italic": false},{"text": "秒","color": "gray","italic": false}]]},Enchantments:[{id:"minecraft:unbreaking",lvl:1s}]} 1
item replace entity @s hotbar.1 with magma_cream[custom_name={"text": "吸血魔珠","color": "white","italic": false},lore=[{"text": "✎ 持续向前方发射激光","color": "gray","italic": false},{"text": "✎ 每次命中造成伤害，并为你提供临时血量和力量Ⅰ效果","color": "gray","italic": false},[{"text": "❃ ","color": "gray","italic": false},{"text": "右键","color": "white","italic": false},{"text": "使用","color": "gray","italic": false}],[{"text": "❃ ","color": "gray","italic": false},{"text": "13","color": "white","italic": false},{"text": " 秒冷却","color": "gray","italic": false}]],enchantments={unbreaking:1},tooltip_display={hidden_components:["enchantments"]}] 1

scoreboard players set @s job_4_2_time 0
scoreboard players set @s job_4_2_time_t 7
scoreboard players set @s job_4_2_x 0