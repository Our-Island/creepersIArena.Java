#clear @s birch_button{display:{Name:{"text": "即将冷却完毕","color": "white","italic": false},Lore:[{"text": "6-技能","color":"gray","italic":false}]}}

clear @s birch_button[custom_name={"text": "即将冷却完毕","color": "white","italic": false},lore=[{"text": "6-技能","color": "gray","italic": false}]]

#$item replace entity @s hotbar.5 with birch_button{display:{Name:"{\"text\": \"即将冷却完毕\",\"color\": \"white\",\"italic\": false}",Lore:["{\"text\": \"6-技能\",\"color\":\"gray\",\"italic\":false}"]}} $(cd_6_t)

$item replace entity @s hotbar.5 with birch_button[custom_name={"text": "即将冷却完毕","color": "white","italic": false},lore=[{"text": "6-技能","color": "gray","italic": false}]] $(cd_6_t)