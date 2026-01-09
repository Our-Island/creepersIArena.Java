#clear @s stone_button{display:{Name:{"text": "冷却中","color": "white","italic": false},Lore:[{"text": "3-技能","color":"gray","italic":false}]}}

clear @s stone_button[custom_name={"text": "冷却中","color": "white","italic": false},lore=[{"text": "3-技能","color": "gray","italic": false}]]

#$item replace entity @s hotbar.2 with stone_button{display:{Name:"{\"text\": \"冷却中\",\"color\": \"white\",\"italic\": false}",Lore:["{\"text\": \"3-技能\",\"color\":\"gray\",\"italic\":false}"]}} $(cd_3)

$item replace entity @s hotbar.2 with stone_button[custom_name={"text": "冷却中","color": "white","italic": false},lore=[{"text": "3-技能","color": "gray","italic": false}]] $(cd_3)