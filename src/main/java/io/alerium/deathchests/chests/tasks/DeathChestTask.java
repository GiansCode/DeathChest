package io.alerium.deathchests.chests.tasks;

import io.alerium.deathchests.DeathChestsPlugin;
import io.alerium.deathchests.chests.ChestManager;
import io.alerium.deathchests.chests.objects.DeathChest;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class DeathChestTask extends BukkitRunnable {

    private final DeathChestsPlugin plugin;
    private final ChestManager chestManager;

    @Override
    public void run() {
        List<UUID> toBreak = new ArrayList<>();
        for (Map.Entry<UUID, DeathChest> entry : chestManager.getDeathChests().entrySet()) {
            if (entry.getValue().getSpawnTime() + chestManager.getDespawnTime() > System.currentTimeMillis())
                continue;

            toBreak.add(entry.getKey());
        }

        if (toBreak.isEmpty())
            return;

        Bukkit.getScheduler().runTask(plugin, () -> toBreak.forEach(chestManager::breakDeathChest));
    }

}
