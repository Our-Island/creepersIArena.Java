clear @s lime_dye
clear @s gray_dye





#老版本
#item replace entity @s[scores={steal_ready=0}] hotbar.0 with gray_dye{display:{Name:[{"text": "未准备","color": "white","italic": false}],Lore:[[{"text": "✎ 游戏开始后会成为旁观者","color": "gray","italic": false}],[{"text": "❃ ","color": "gray","italic": false},{"text": "右键","color": "white","italic": false},{"text": "进行准备","color": "gray","italic": false}]]}} 1
#item replace entity @s[scores={steal_ready=1}] hotbar.0 with lime_dye{display:{Name:[{"text": "已准备","color": "white","italic": false}],Lore:[[{"text": "✎ 游戏开始后会参与游戏","color": "gray","italic": false}],[{"text": "❃ ","color": "gray","italic": false},{"text": "右键","color": "white","italic": false},{"text": "取消准备","color": "gray","italic": false}]]}} 1

item replace entity @s[scores={steal_ready=0}] hotbar.0 with gray_dye[custom_name={"text": "未准备","color": "white","italic": false},lore=[{"text": "✎ 游戏开始后会成为旁观者","color": "gray","italic": false},[{"text": "❃ ","color": "gray","italic": false},{"text": "右键","color": "white","italic": false},{"text": "进行准备","color": "gray","italic": false}]],consumable={consume_seconds:0,animation:"none",has_consume_particles:false,on_consume_effects:[],sound:"minecraft:ui.hud.bubble_pop"}] 1

item replace entity @s[scores={steal_ready=1}] hotbar.0 with lime_dye[custom_name={"text": "已准备","color": "white","italic": false},lore=[{"text": "✎ 游戏开始后会参与游戏","color": "gray","italic": false},[{"text": "❃ ","color": "gray","italic": false},{"text": "右键","color": "white","italic": false},{"text": "取消准备","color": "gray","italic": false}]],consumable={consume_seconds:0,animation:"none",has_consume_particles:false,on_consume_effects:[],sound:"minecraft:ui.hud.bubble_pop"}] 1

