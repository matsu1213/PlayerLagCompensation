package net.azisaba.playerlagcompensation;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ToggleVelocityCompensationCommand implements CommandExecutor {

    public static boolean shouldVelocityCompensate = true;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(shouldVelocityCompensate){
            sender.sendMessage("ノックバックラグ補償を無効にしました。");
            shouldVelocityCompensate = false;
        }else {
            sender.sendMessage("ノックバックラグ補償を有効にしました。");
            shouldVelocityCompensate = true;
        }
        return true;
    }

}
