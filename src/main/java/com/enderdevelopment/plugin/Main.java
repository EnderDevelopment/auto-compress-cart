package com.enderdevelopment.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class Main extends JavaPlugin implements Listener {
    private final Map<Material, Material> recipes = new HashMap<>();
    private final Set<Material> enabled = new HashSet<>();
    private final Map<Player, Inventory> menus = new HashMap<>();
    private NamespacedKey cartKey;
    private Component title;

    @Override public void onEnable() {
        cartKey = new NamespacedKey(this, "compress_cart");
        title = Component.text("Auto-Compress Settings", NamedTextColor.GOLD);
        recipe(Material.COBBLESTONE, Material.STONE); recipe(Material.RAW_IRON, Material.IRON_INGOT);
        recipe(Material.RAW_GOLD, Material.GOLD_INGOT); recipe(Material.RAW_COPPER, Material.COPPER_INGOT);
        recipe(Material.WHEAT, Material.BREAD); recipe(Material.SUGAR_CANE, Material.SUGAR);
        recipe(Material.OAK_LOG, Material.OAK_PLANKS); recipe(Material.SPRUCE_LOG, Material.SPRUCE_PLANKS);
        recipe(Material.BIRCH_LOG, Material.BIRCH_PLANKS); recipe(Material.JUNGLE_LOG, Material.JUNGLE_PLANKS);
        recipe(Material.ACACIA_LOG, Material.ACACIA_PLANKS); recipe(Material.DARK_OAK_LOG, Material.DARK_OAK_PLANKS);
        recipe(Material.MANGROVE_LOG, Material.MANGROVE_PLANKS); recipe(Material.CHERRY_LOG, Material.CHERRY_PLANKS);
        recipe(Material.BAMBOO_BLOCK, Material.BAMBOO_PLANKS);
        List<String> saved = getConfig().getStringList("enabled");
        for (Material input : recipes.keySet()) if (saved.isEmpty() || saved.contains(input.name())) enabled.add(input);
        Bukkit.getPluginManager().registerEvents(this, this);
    }
    private void recipe(Material input, Material output) { recipes.put(input, output); }
    @Override public void onDisable() { saveEnabled(); }
    private void saveEnabled() { getConfig().set("enabled", enabled.stream().map(Enum::name).toList()); saveConfig(); }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Players only."); return true; }
        if (args.length > 0 && args[0].equalsIgnoreCase("cart")) {
            ItemStack token = new ItemStack(Material.MINECART); ItemMeta meta = token.getItemMeta();
            meta.displayName(Component.text("Auto-Compress Minecart Token", NamedTextColor.GOLD));
            meta.lore(List.of(Component.text("Hold in offhand, then right-click a minecart.", NamedTextColor.GRAY)));
            meta.getPersistentDataContainer().set(cartKey, PersistentDataType.BYTE, (byte) 1); token.setItemMeta(meta);
            Map<Integer, ItemStack> rest = player.getInventory().addItem(token);
            rest.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
            player.sendMessage(Component.text("Token received.", NamedTextColor.GREEN)); return true;
        }
        openMenu(player); return true;
    }
    private void openMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, title); int slot = 10;
        for (Material input : recipes.keySet()) { inv.setItem(slot, setting(input)); slot++; if (slot == 17) slot = 19; if (slot == 26) slot = 28; if (slot == 35) slot = 37; }
        ItemStack info = new ItemStack(Material.BOOK); ItemMeta m = info.getItemMeta();
        m.displayName(Component.text("Click a material to toggle it", NamedTextColor.AQUA)); info.setItemMeta(m); inv.setItem(49, info);
        menus.put(player, inv); player.openInventory(inv);
    }
    private ItemStack setting(Material input) {
        ItemStack item = new ItemStack(input); ItemMeta meta = item.getItemMeta(); boolean active = enabled.contains(input);
        meta.displayName(Component.text(input.name().replace('_', ' '), active ? NamedTextColor.GREEN : NamedTextColor.RED));
        meta.lore(List.of(Component.text("Output: " + recipes.get(input).name().replace('_', ' '), NamedTextColor.GRAY), Component.text(active ? "Enabled - click to disable" : "Disabled - click to enable", active ? NamedTextColor.GREEN : NamedTextColor.RED)));
        item.setItemMeta(meta); return item;
    }
    @EventHandler public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return; Inventory inv = menus.get(player);
        if (inv == null || event.getView().getTopInventory() != inv) return; event.setCancelled(true);
        ItemStack item = event.getCurrentItem(); if (item == null || !recipes.containsKey(item.getType())) return;
        Material input = item.getType(); if (!enabled.remove(input)) enabled.add(input); saveEnabled();
        inv.setItem(event.getRawSlot(), setting(input));
    }
    @EventHandler public void onMenuClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player && menus.get(player) == event.getInventory()) menus.remove(player);
    }
    @EventHandler public void onBlockDrops(BlockDropItemEvent event) {
        Material input = event.getBlockState().getType(); if (!enabled.contains(input)) return;
        Minecart cart = nearbyCart(event.getPlayer());
        if (cart == null) return;
        Material output = recipes.get(input); if (output == null) return;
        int count = 0; for (org.bukkit.entity.Item dropped : event.getItems()) {
            if (dropped.getItemStack().getType() == input) { count += dropped.getItemStack().getAmount(); dropped.remove(); }
        }
        if (count > 0) event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(output, count));
    }
    private Minecart nearbyCart(Player player) {
        for (Entity entity : player.getNearbyEntities(6, 4, 6)) if (entity instanceof Minecart cart && cart.getPersistentDataContainer().has(cartKey, PersistentDataType.BYTE)) return cart;
        return null;
    }
    @EventHandler public void onCartInteract(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || !(event.getRightClicked() instanceof Minecart cart)) return;
        ItemStack offhand = event.getPlayer().getInventory().getItemInOffHand(); ItemMeta meta = offhand.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(cartKey, PersistentDataType.BYTE)) return;
        event.setCancelled(true); cart.getPersistentDataContainer().set(cartKey, PersistentDataType.BYTE, (byte) 1);
        offhand.setAmount(offhand.getAmount() - 1); event.getPlayer().sendMessage(Component.text("Minecart enabled for auto-compression. Keep it within 6 blocks while mining.", NamedTextColor.GREEN));
    }
}
