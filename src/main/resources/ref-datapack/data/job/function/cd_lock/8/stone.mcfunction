#clear @s stone_button{display:{Name:{"text": "冷却中","color": "white","italic": false},Lore:[{"text": "8-技能","color":"gray","italic":false}]}}

clear @s stone_button[custom_name={"text": "冷却中","color": "white","italic": false},lore=[{"text": "8-技能","color": "gray","italic": false}]]

#$item replace entity @s hotbar.7 with stone_button{display:{Name:"{\"text\": \"冷却中\",\"color\": \"white\",\"italic\": false}",Lore:["{\"text\": \"8-技能\",\"color\":\"gray\",\"italic\":false}"]}} $(cd_8)

$item replace entity @s hotbar.7 with stone_button[custom_name={"text": "冷却中","color": "white","italic": false},lore=[{"text": "8-技能","color": "gray","italic": false}]] $(cd_8)