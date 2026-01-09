execute unless entity @s[nbt={SelectedItem:{id:"minecraft:iron_sword",count:1}}] unless entity @s[nbt={SelectedItem:{id:"minecraft:golden_hoe",count:1}}] unless entity @s[nbt={SelectedItem:{id:"minecraft:diamond_axe",count:1}}] unless entity @s[nbt={SelectedItem:{id:"minecraft:brush",count:1}}] unless entity @s[nbt={SelectedItem:{id:"minecraft:fishing_rod",count:1}}] run scoreboard players set @a[advancements={ murder:accident/2_direct_hit=true}] murder_type 0021000

execute unless entity @s[nbt={SelectedItem:{id:"minecraft:iron_sword",count:1}}] unless entity @s[nbt={SelectedItem:{id:"minecraft:golden_hoe",count:1}}] unless entity @s[nbt={SelectedItem:{id:"minecraft:diamond_axe",count:1}}] unless entity @s[nbt={SelectedItem:{id:"minecraft:brush",count:1}}] unless entity @s[nbt={SelectedItem:{id:"minecraft:fishing_rod",count:1}}] run scoreboard players operation @a[advancements={ murder:accident/2_direct_hit=true},limit=1] murder_last_source = @s id

execute unless entity @s[nbt={SelectedItem:{id:"minecraft:iron_sword",count:1}}] unless entity @s[nbt={SelectedItem:{id:"minecraft:golden_hoe",count:1}}] unless entity @s[nbt={SelectedItem:{id:"minecraft:diamond_axe",count:1}}] unless entity @s[nbt={SelectedItem:{id:"minecraft:brush",count:1}}] unless entity @s[nbt={SelectedItem:{id:"minecraft:fishing_rod",count:1}}] run scoreboard players set @a[advancements={ murder:accident/2_direct_hit=true},limit=1] murder_time_to_reset 600

advancement revoke @a only murder:accident/2_direct_hit_source
advancement revoke @a only murder:accident/2_direct_hit

