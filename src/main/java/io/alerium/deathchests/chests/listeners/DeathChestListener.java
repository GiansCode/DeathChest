package io.alerium.deathchests.chests.listeners;

import com.live.bemmamin.gps.api.events.GPSStopEvent;
import io.alerium.deathchests.DeathChestsPlugin;
import io.alerium.deathchests.chests.ChestManager;
import io.alerium.deathchests.chests.objects.DeathChest;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class DeathChestListener implements Listener {

    private final DeathChestsPlugin plugin;
    private final ChestManager chestManager;

    private final Map<UUID, String> activeGPS = new HashMap<>();

    @EventHandler
    public void onPlayerDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;

        if (!hasDeathChest(player.getInventory()))
            return;

        event.getDrops().clear();
        chestManager.createDeathChest(player.getLocation(), player);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        chestManager.getDeathChest(event.getPlayer().getUniqueId()).ifPresent(deathChest -> {
            Player player = event.getPlayer();
            if (deathChest.getLocation().getWorld() != player.getWorld())
                return;

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                String pointID = player.getUniqueId() + "_death_chest";
                plugin.getGpsapi().addPoint(pointID, deathChest.getLocation());
                plugin.getGpsapi().startCompass(player, pointID);
                activeGPS.put(player.getUniqueId(), pointID);
            }, 5);
        });
    }

    @EventHandler
    public void onGPSStop(GPSStopEvent event) {
        if (!activeGPS.containsKey(event.getPlayer().getUniqueId()))
            return;

        plugin.getGpsapi().removePoint(activeGPS.get(event.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!activeGPS.containsKey(event.getPlayer().getUniqueId()))
            return;

        plugin.getGpsapi().stopGPS(event.getPlayer());
        plugin.getGpsapi().removePoint(activeGPS.get(event.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.CHEST)
            return;

        chestManager.getDeathChest(block.getLocation()).ifPresent(deathChest -> {
            event.setCancelled(true);
            if (!deathChest.getOwner().equals(event.getPlayer().getUniqueId()))
                event.getPlayer().sendMessage(plugin.getConfiguration().getMessage("messages.cannotOpenDeathChest"));
            else
                event.getPlayer().openInventory(deathChest.getInventory());
        });
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().isEmpty())
            return;

        if (!(event.getInventory().getHolder() instanceof DeathChest deathChest))
            return;

        chestManager.breakDeathChest(deathChest.getOwner());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType() != chestManager.getDeathChestItem().getType())
            return;

        ItemStack item = event.getItemInHand();
        ItemMeta meta = item.getItemMeta();
        if (meta.getPersistentDataContainer().has(chestManager.getDeathChestKey(), PersistentDataType.INTEGER))
            event.setCancelled(true);
    }

    private boolean hasDeathChest(PlayerInventory inventory) {
        for (ItemStack item : inventory) {
            if (item == null || item.getType() != chestManager.getDeathChestItem().getType())
                continue;

            ItemMeta meta = item.getItemMeta();
            if (meta.getPersistentDataContainer().has(chestManager.getDeathChestKey(), PersistentDataType.INTEGER)) {
                item.setAmount(item.getAmount()-1);
                return true;
            }
        }

        return false;
    }

}
