package io.alerium.deathchests.chests.commands;

import io.alerium.deathchests.DeathChestsPlugin;
import io.alerium.deathchests.chests.ChestManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class DeathChestCommand implements CommandExecutor {

    private final DeathChestsPlugin plugin;
    private final ChestManager chestManager;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getConfiguration().getMessage("messages.usage"));
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null || !player.isOnline()) {
            sender.sendMessage(plugin.getConfiguration().getMessage("messages.playerOffline"));
            return true;
        }

        try {
            int amount = Integer.parseInt(args[1]);
            if (amount <= 0) {
                sender.sendMessage(plugin.getConfiguration().getMessage("messages.invalidAmount"));
                return true;
            }

            player.getInventory().addItem(chestManager.getDeathChestItem().asQuantity(amount));
            sender.sendMessage(plugin.getConfiguration().getMessage("messages.commandExecuted"));
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getConfiguration().getMessage("messages.invalidAmount"));
        }
        return true;
    }

}
