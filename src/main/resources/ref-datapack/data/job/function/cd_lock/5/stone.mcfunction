#clear @s stone_button{display:{Name:{"text": "冷却中","color": "white","italic": false},Lore:[{"text": "5-技能","color":"gray","italic":false}]}}

clear @s stone_button[custom_name={"text": "冷却中","color": "white","italic": false},lore=[{"text": "5-技能","color": "gray","italic": false}]]

#$item replace entity @s hotbar.4 with stone_button{display:{Name:"{\"text\": \"冷却中\",\"color\": \"white\",\"italic\": false}",Lore:["{\"text\": \"5-技能\",\"color\":\"gray\",\"italic\":false}"]}} $(cd_5)

$item replace entity @s hotbar.4 with stone_button[custom_name={"text": "冷却中","color": "white","italic": false},lore=[{"text": "5-技能","color": "gray","italic": false}]] $(cd_5)