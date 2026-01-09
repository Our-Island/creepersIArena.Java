#clear @s birch_button{display:{Name:{"text": "即将冷却完毕","color": "white","italic": false},Lore:[{"text": "3-技能","color":"gray","italic":false}]}}

clear @s birch_button[custom_name={"text": "即将冷却完毕","color": "white","italic": false},lore=[{"text": "3-技能","color": "gray","italic": false}]]

#$item replace entity @s hotbar.2 with birch_button{display:{Name:"{\"text\": \"即将冷却完毕\",\"color\": \"white\",\"italic\": false}",Lore:["{\"text\": \"3-技能\",\"color\":\"gray\",\"italic\":false}"]}} $(cd_3_t)

$item replace entity @s hotbar.2 with birch_button[custom_name={"text": "即将冷却完毕","color": "white","italic": false},lore=[{"text": "3-技能","color": "gray","italic": false}]] $(cd_3_t)