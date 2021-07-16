package io.alerium.deathchests;

import com.live.bemmamin.gps.api.GPSAPI;
import io.alerium.deathchests.chests.ChestManager;
import io.alerium.deathchests.utils.Configuration;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class DeathChestsPlugin extends JavaPlugin {

    @Getter private Configuration configuration;
    @Getter private GPSAPI gpsapi;
    private ChestManager chestManager;

    @Override
    public void onEnable() {
        configuration = new Configuration(this, "config");
        gpsapi = new GPSAPI(this);

        chestManager = new ChestManager(this);
        chestManager.enable();
    }

    @Override
    public void onDisable() {
        chestManager.disable();
        gpsapi.removeAllPoints();
    }

}
