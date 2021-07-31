package io.alerium.deathchests.utils;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Configuration {

    private final Plugin plugin;
    private final File file;

    @Getter private FileConfiguration config;

    public Configuration(Plugin plugin, String name) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), name + ".yml");

        if (!file.exists())
            plugin.saveResource(file.getName(), false);

        config = YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred while saving the config file.");
        }
    }

    public ItemStack getItemStack(String path, String... placeholders) {
        ItemStack item = new ItemStack(Material.valueOf(config.getString(path + ".material")));
        ItemMeta meta = item.getItemMeta();
        meta.displayName(getMessage(path + ".name", placeholders));
        meta.lore(getMessageList(path + ".lore", placeholders));
        item.setItemMeta(meta);
        return item;
    }

    public Component getMessage(String path, String... placeholders) {
        return parsePlaceholders(config.getString(path), placeholders);
    }

    public List<Component> getMessageList(String path, String... placeholders) {
        return config.getStringList(path).stream().map(s -> parsePlaceholders(s, placeholders)).collect(Collectors.toList());
    }

    private Component parsePlaceholders(String s, String... placeholders) {
        for (int i = 0; i < placeholders.length; i += 2)
            s = s.replaceAll(placeholders[i], placeholders[i+1]);

        if (ChatColor.stripColor(s).equals(s))
            return MiniMessage.get().parse(s);
        return LegacyComponentSerializer.legacy('&').deserialize(s);
    }

}
