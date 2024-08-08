package net.azisaba.playerlagcompensation;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length > 0 && args[0].equals("reload")){
            PlayerLagCompensation.INSTANCE.getConfigManager().reload();
            sender.sendMessage("[PlayerLagCompensation] Config reloaded.");
        }
        return true;
    }
}
