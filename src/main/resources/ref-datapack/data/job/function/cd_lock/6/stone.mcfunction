#clear @s stone_button{display:{Name:"{\"text\": \"冷却中\",\"color\": \"white\",\"italic\": false}",Lore:["{\"text\": \"6-技能\",\"color\":\"gray\",\"italic\":false}"]}}

clear @s stone_button[custom_name={"text": "冷却中","color": "white","italic": false},lore=[{"text": "6-技能","color": "gray","italic": false}]]

#$item replace entity @s hotbar.5 with stone_button{display:{Name:"{\"text\": \"冷却中\",\"color\": \"white\",\"italic\": false}",Lore:["{\"text\": \"6-技能\",\"color\":\"gray\",\"italic\":false}"]}} $(cd_6)

$item replace entity @s hotbar.5 with stone_button[custom_name={"text": "冷却中","color": "white","italic": false},lore=[{"text": "6-技能","color": "gray","italic": false}]] $(cd_6)