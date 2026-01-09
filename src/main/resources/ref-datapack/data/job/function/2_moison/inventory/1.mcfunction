clear @s dispenser


#item replace entity @s hotbar.0 with dispenser{HideFlags:255,display:{Name:[{"text": "机关","color": "white","italic": false}],Lore:[[{"text": "✎ 向前方射出一根箭","color": "gray","italic": false}],[{"text": "❃ ","color": "gray","italic": false},{"text": "右键","color": "white","italic": false},{"text": "使用 │ 冷却","color": "gray","italic": false},{"text": " 2.5 ","color": "white","italic": false},{"text": "秒","color": "gray","italic": false}]]}} 1


item replace entity @s hotbar.0 with dispenser[custom_name={"text": "吹箭机","color": "white","italic": false},lore=[{"text": "✎ 向前方发射一根箭","color": "gray","italic": false},{"text": "✎ 命中敌人造成伤害","color": "gray","italic": false},[{"text": "❃ ","color": "gray","italic": false},{"text": "右键","color": "white","italic": false},{"text": "使用","color": "gray","italic": false}],[{"text": "❃ ","color": "gray","italic": false},{"text": "1.5","color": "white","italic": false},{"text": " 秒冷却","color": "gray","italic": false}],{"text": "✎ 每当[吹箭机]命中敌人，减少2秒冷却","color": "gray","italic": false}],consumable={consume_seconds:0,animation:"none",has_consume_particles:false,on_consume_effects:[],sound:"minecraft:ui.hud.bubble_pop"}] 1