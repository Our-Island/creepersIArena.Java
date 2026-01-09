#检测背包对应格子是否正确



execute as @s[scores={steal_ready=0},nbt=!{Inventory:[{count: 1, Slot: 0b, components: {"minecraft:custom_name": {"color":"white","italic":false,"text":"未准备"}, "minecraft:lore": [{"color":"gray","italic":false,"text":"✎ 游戏开始后会成为旁观者"}, {"color":"gray","extra":[{"color":"white","italic":false,"text":"右键"},{"color":"gray","italic":false,"text":"进行准备"}],"italic":false,"text":"❃ "}]}, id: "minecraft:gray_dye"}]}] unless score $tool game_stage matches 1.. run function choosejob:inventory/-1

execute as @s[scores={steal_ready=1},nbt=!{Inventory:[{count: 1, Slot: 0b, components: {"minecraft:custom_name": {"color":"white","italic":false,"text":"已准备"}, "minecraft:lore": [{"color":"gray","italic":false,"text":"✎ 游戏开始后会参与游戏"}, {"color":"gray","extra":[{"color":"white","italic":false,"text":"右键"},{"color":"gray","italic":false,"text":"取消准备"}],"italic":false,"text":"❃ "}]}, id: "minecraft:lime_dye"}]}] unless score $tool game_stage matches 1.. run function choosejob:inventory/-1