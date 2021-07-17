package io.alerium.deathchests.chests.commands;

import io.alerium.deathchests.DeathChestsPlugin;
import io.alerium.deathchests.chests.ChestManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;

@RequiredArgsConstructor
public class DeathChestCommand implements CommandExecutor {

    private final DeathChestsPlugin plugin;
    private final ChestManager chestManager;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            plugin.getConfiguration().getMessageList("messages.commands.help").forEach(sender::sendMessage);
            return true;
        }

        String subcommand = args[0];
        switch (subcommand.toLowerCase()) {
            case "reload" -> {
                reload(sender);
                return true;
            }
            case "give" -> {
                give(sender, Arrays.copyOfRange(args, 1, args.length));
                return true;
            }
            case "giveall" -> {
                giveAll(sender, Arrays.copyOfRange(args, 1, args.length));
                return true;
            }
            default -> {
                plugin.getConfiguration().getMessageList("messages.commands.help").forEach(sender::sendMessage);
                return true;
            }
        }
    }

    private void reload(CommandSender sender) {
        plugin.getConfiguration().reload();
        chestManager.reload();
        sender.sendMessage(plugin.getConfiguration().getMessage("messages.commands.reload"));
    }

    private void give(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getConfiguration().getMessage("messages.commands.give.usage"));
            return;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null || !player.isOnline()) {
            sender.sendMessage(plugin.getConfiguration().getMessage("messages.commands.give.playerOffline"));
            return;
        }

        try {
            int amount = Integer.parseInt(args[1]);
            if (amount <= 0) {
                sender.sendMessage(plugin.getConfiguration().getMessage("messages.commands.give.invalidAmount"));
                return;
            }

            player.getInventory().addItem(chestManager.getDeathChestItem().asQuantity(amount));
            sender.sendMessage(plugin.getConfiguration().getMessage("messages.commands.give.success"));
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getConfiguration().getMessage("messages.commands.give.invalidAmount"));
        }
    }

    private void giveAll(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(plugin.getConfiguration().getMessage("messages.commands.giveAll.usage"));
            return;
        }

        try {
            int amount = Integer.parseInt(args[1]);
            if (amount <= 0) {
                sender.sendMessage(plugin.getConfiguration().getMessage("messages.commands.giveAll.invalidAmount"));
                return;
            }

            ItemStack item = chestManager.getDeathChestItem().asQuantity(amount);
            for (Player player : Bukkit.getOnlinePlayers())
                player.getInventory().addItem(item);

            sender.sendMessage(plugin.getConfiguration().getMessage("messages.commands.giveAll.success"));
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getConfiguration().getMessage("messages.commands.giveAll.invalidAmount"));
        }
    }

}
