package io.alerium.deathchests.chests.objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

@RequiredArgsConstructor @Getter
public class DeathChest {

    private final UUID owner;
    private final Location location;
    private final long spawnTime;

    private final Inventory inventory;

}
