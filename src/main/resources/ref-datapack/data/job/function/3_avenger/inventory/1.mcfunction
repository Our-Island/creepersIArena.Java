clear @s iron_sword


#item replace entity @s[scores={HP=11..}] hotbar.0 with iron_sword{HideFlags:255,display:{Name:[{"text": "愤怒之刃","color": "white","italic": false}],Lore:[[{"text": "✎ 伤害 4 / 攻速 1.4","color": "gray","italic": false}],[{"text": "❃ ","color": "gray","italic": false},{"text": "左键","color": "white","italic": false},{"text": "使用","color": "gray","italic": false}]]},AttributeModifiers:[{Amount:3d, AttributeName:"minecraft:attack_damage",id:"1",Operation:0,Slot:"mainhand",UUID:[I;1000,1000,1000,1000]},{Amount:-2.6d, AttributeName:"minecraft:attack_speed",Operation:0,Slot:"mainhand",UUID:[I;1000,1000,1000,1001]}],Unbreakable:1b} 1

item replace entity @s[scores={HP=11..}] hotbar.0 with iron_sword[custom_name={"text": "愤怒之刃","color": "white","italic": false},lore=[{"text": "✎ 伤害 4 / 攻速 1.4","color": "gray","italic": false},[{"text": "❃ ","color": "gray","italic": false},{"text": "左键","color": "white","italic": false},{"text": "使用","color": "gray","italic": false}]],tooltip_display={hidden_components:["attribute_modifiers","can_place_on","can_break","enchantments","fireworks","firework_explosion","trim","dyed_color","unbreakable","charged_projectiles"]},unbreakable={},attribute_modifiers=[{amount:3,id:"1",operation:"add_value",type:"minecraft:attack_damage",slot:"any",id:"a:a"},{amount:-2.6,operation:"add_value",type:"minecraft:attack_speed",slot:"any",id:"a:a"}]] 1



#item replace entity @s[scores={HP=..10}] hotbar.0 with iron_sword{HideFlags:255,display:{Name:[{"text": "复仇之刃","color": "white","italic": false}],Lore:[[{"text": "✎ 伤害 7 / 攻速 1.4 / 暴击仅额外 + 0.5","color": "gray","italic": false}],[{"text": "❃ ","color": "gray","italic": false},{"text": "左键","color": "white","italic": false},{"text": "使用","color": "gray","italic": false}]]},AttributeModifiers:[{Amount:0d, AttributeName:"minecraft:attack_damage",id:"1",Operation:0,Slot:"mainhand",UUID:[I;1000,1000,1000,1000]},{Amount:-2.6d, AttributeName:"minecraft:attack_speed",Operation:0,Slot:"mainhand",UUID:[I;1000,1000,1000,1001]}],Enchantments:[{id:"minecraft:sharpness",lvl:11s}],Unbreakable:1b} 1

item replace entity @s[scores={HP=..10}] hotbar.0 with iron_sword[custom_name={"text": "复仇之刃","color": "white","italic": false},lore=[{"text": "✎ 伤害 7 / 攻速 1.4 / 暴击仅额外 + 0.5","color": "gray","italic": false},[{"text": "❃ ","color": "gray","italic": false},{"text": "左键","color": "white","italic": false},{"text": "使用","color": "gray","italic": false}]],tooltip_display={hidden_components:["attribute_modifiers","can_place_on","can_break","enchantments","fireworks","firework_explosion","trim","dyed_color","unbreakable","charged_projectiles"]},unbreakable={},attribute_modifiers=[{amount:0,id:"1",operation:"add_value",type:"minecraft:attack_damage",slot:"any",id:"a:a"},{amount:-2.6,operation:"add_value",type:"minecraft:attack_speed",slot:"any",id:"a:a"}],enchantments={sharpness:11}] 1





