package io.alerium.deathchests.chests;

import io.alerium.deathchests.DeathChestsPlugin;
import io.alerium.deathchests.chests.commands.DeathChestCommand;
import io.alerium.deathchests.chests.listeners.DeathChestListener;
import io.alerium.deathchests.chests.objects.DeathChest;
import io.alerium.deathchests.chests.tasks.DeathChestTask;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class ChestManager {

    private final DeathChestsPlugin plugin;
    @Getter private final Map<UUID, DeathChest> deathChests = new HashMap<>();

    @Getter private NamespacedKey deathChestKey;
    @Getter private ItemStack deathChestItem;
    @Getter private long despawnTime;

    public void enable() {
        deathChestKey = new NamespacedKey(plugin, "death_chest");
        reload();

        Bukkit.getPluginManager().registerEvents(new DeathChestListener(plugin, this), plugin);
        new DeathChestTask(plugin, this).runTaskTimerAsynchronously(plugin, 20 * 5, 20 * 5);
        plugin.getCommand("deathchest").setExecutor(new DeathChestCommand(plugin, this));
    }

    public void disable() {
        for (UUID uuid : deathChests.keySet().toArray(new UUID[0]))
            breakDeathChest(uuid);
    }

    public void reload() {
        deathChestItem = plugin.getConfiguration().getItemStack("deathChestItem");
        ItemMeta meta = deathChestItem.getItemMeta();
        meta.getPersistentDataContainer().set(deathChestKey, PersistentDataType.INTEGER, 1);
        deathChestItem.setItemMeta(meta);

        despawnTime = TimeUnit.SECONDS.toMillis(plugin.getConfiguration().getConfig().getInt("chestDespawnTime"));
    }

    public Optional<DeathChest> getDeathChest(UUID uuid) {
        return Optional.ofNullable(deathChests.get(uuid));
    }

    public Optional<DeathChest> getDeathChest(Location location) {
        for (DeathChest deathChest : deathChests.values()) {
            if (deathChest.getLocation().getWorld() == location.getWorld() && deathChest.getLocation().getBlockX() == location.getBlockX() && deathChest.getLocation().getBlockY() == location.getBlockY() && deathChest.getLocation().getBlockZ() == location.getBlockZ())
                return Optional.of(deathChest);
        }

        return Optional.empty();
    }

    public void breakDeathChest(UUID uuid) {
        getDeathChest(uuid).ifPresent(deathChest -> {
            Block block = deathChest.getLocation().getBlock();
            block.setType(Material.AIR);

            // I've to create a new ArrayList to fix a ConcurrentModificationException
            new ArrayList<>(deathChest.getInventory().getViewers()).forEach(HumanEntity::closeInventory);

            for (ItemStack item : deathChest.getInventory().getContents()) {
                if (item == null || item.getType() == Material.AIR)
                    continue;

                block.getWorld().dropItemNaturally(deathChest.getLocation(), item);
            }

            deathChests.remove(uuid);
        });
    }

    public void createDeathChest(Location loc, Player player) {
        breakDeathChest(player.getUniqueId());

        Block block = getSafeLocation(loc).getBlock();
        block.setType(Material.CHEST);

        DeathChest deathChest = new DeathChest(player.getUniqueId(), block.getLocation(), System.currentTimeMillis());
        Inventory inventory = Bukkit.createInventory(deathChest, 9 * 5, plugin.getConfiguration().getMessage("deathChestTitle", "%player%", player.getName()));
        inventory.setContents(player.getInventory().getContents());
        inventory.addItem(player.getInventory().getArmorContents());
        player.getInventory().clear();
        deathChest.setInventory(inventory);

        deathChests.put(player.getUniqueId(), deathChest);
    }

    private Location getSafeLocation(Location loc) {
        if (loc.getBlock().getType() == Material.AIR)
            return loc;

        return getSafeLocation(loc.clone().add(0, 1, 0));
    }

}
