package net.azisaba.playerlagcompensation;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class CompensatedCountDownCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        CompensatedCountDown task = new CompensatedCountDown(200, new ArrayList<>(Bukkit.getOnlinePlayers()), () -> {
            //Bukkit.broadcastMessage("§e§lGO!");
        });

        task.runTaskTimer(PlayerLagCompensation.INSTANCE, 0, 1);

        return true;
    }
}
