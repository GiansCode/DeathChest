package io.alerium.deathchests.chests.objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

@RequiredArgsConstructor @Getter
public class DeathChest implements InventoryHolder {

    private final UUID owner;
    private final Location location;
    private final long spawnTime;

    @Setter private Inventory inventory;

}
