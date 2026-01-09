clear @s calibrated_sculk_sensor
clear @s sculk_sensor

#item replace entity @s[scores={HP=11..}] armor.head with sculk_sensor{HideFlags:255,display:{Name:[{"text": "面具","color": "white","italic": false}],Lore:[[{"text": "✎ 复仇者用其遮挡面部","color": "gray","italic": false}]]}} 1

item replace entity @s[scores={HP=11..}] armor.head with sculk_sensor[custom_name={"text": "面具","color": "white","italic": false},lore=[{"text": "✎ 压制着怒火","color": "gray","italic": false}]] 1

#item replace entity @s[scores={HP=..10}] armor.head with calibrated_sculk_sensor{HideFlags:255,display:{Name:[{"text": "复仇面具","color": "white","italic": false}],Lore:[[{"text": "✎ 象征着复仇者的愤怒","color": "gray","italic": false}]]}} 1

item replace entity @s[scores={HP=..10}] armor.head with calibrated_sculk_sensor[custom_name={"text": "复仇面具","color": "white","italic": false},lore=[{"text": "✎ 不灭......","color": "gray","italic": false}]] 1