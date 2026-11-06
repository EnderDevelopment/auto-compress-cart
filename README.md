# AutoCompress Cart Minecraft Plugin

AutoCompress Cart is a Minecraft plugin for Paper servers that automatically converts selected block drops into configured output materials when a player mines near an enabled minecart. It includes a custom GUI for toggling compression recipes and an offhand minecart token system for applying auto-compression to minecarts.

## Install

1. Use Java 21 and a Paper 1.21.11 server.
2. Place the compiled plugin JAR in the server's `plugins` folder.
3. Start or restart the server.
4. Confirm that AutoCompress loads successfully.
5. The plugin creates its configuration data after saving enabled recipe settings.

## Use

1. Run `/compress` to open the Auto-Compress Settings GUI.
2. Click a material to enable or disable its compression recipe.
3. Enabled materials appear in green; disabled materials appear in red.
4. Run `/compress cart` to receive an Auto-Compress Minecart Token.
5. Hold the token in your offhand.
6. Right-click a minecart while using your main hand to apply auto-compression to that minecart.
7. Keep the enabled minecart within 6 blocks horizontally and 4 blocks vertically while mining.
8. When an enabled block is broken near the minecart, matching drops are replaced with the configured output item.

Default recipes include cobblestone to stone, raw ores to ingots, wheat to bread, sugar cane to sugar, logs to planks, and bamboo blocks to bamboo planks.

---

More Minecraft plugin projects by EnderDevelopment: https://github.com/EnderDevelopment

Project page: https://dash.enderdevelopment.com/community/71f6a490-6acf-4f32-b67b-12049f49c425?utm_source=github&utm_medium=readme&utm_campaign=auto_publish&utm_content=71f6a490-6acf-4f32-b67b-12049f49c425
