package net.azisaba.playerlagcompensation;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ToggleCompensationCommand implements CommandExecutor {

    public static boolean shouldCompensate = true;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(shouldCompensate){
            sender.sendMessage("ラグ補償を無効にしました。");
            shouldCompensate = false;
        }else {
            sender.sendMessage("ラグ補償を有効にしました。");
            shouldCompensate = true;
        }
        return true;
    }
}
