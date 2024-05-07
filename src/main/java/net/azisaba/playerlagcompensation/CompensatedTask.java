package net.azisaba.playerlagcompensation;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEventsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CompensatedTask extends BukkitRunnable {

    public int tickCount;
    private final Runnable task;
    private List<Player> players;

    private List<Player> transactionReceivedPlayers = new ArrayList<>();
    private boolean ready = false;
    private HashMap<Player, Integer> lastDesyncTickRun = new HashMap<>();

    public CompensatedTask(int tickCount, List<Player> players, Runnable task) {
        this.tickCount = tickCount;
        this.task = task;
        this.players = players;
    }

    @Override
    public void run() {
        if(tickCount % 7 == 0){
            for(Player player : players){
                GrimPlayer gp = PlayerLagCompensation.INSTANCE.getGrimPlayer(player);
                if(gp != null){
                    gp.sendTransaction();
                    gp.latencyUtils.addRealTimeTask(gp.lastTransactionSent.get(), () -> {
                        PlayerPingUtil.addPing(player.getUniqueId(), gp.getTransactionPing());
                        if(!transactionReceivedPlayers.contains(player)){
                            transactionReceivedPlayers.add(player);
                        }
                        //gp.bukkitPlayer.sendMessage("Ping: " + gp.getTransactionPing());
                    });
                }else {
                    PlayerPingUtil.addPing(player.getUniqueId(), PacketEvents.getAPI().getPlayerManager().getPing(player));
                    if(!transactionReceivedPlayers.contains(player)){
                        transactionReceivedPlayers.add(player);
                    }
                }
            }
        }

        if(players.size() == transactionReceivedPlayers.size() && PlayerPingUtil.isAllPlayersPingStable(players)){
            if(!ready){
                ready = true;
                tickCount = tickCount + PlayerPingUtil.getMostHighestPing(players) / 100;
            }
            for (Player player : players) {
                int desyncTickCount = tickCount - PlayerPingUtil.getPing(player.getUniqueId()) / 100;
                int lastDesyncTick = lastDesyncTickRun.getOrDefault(player, desyncTickCount + 1);
                int skipped = lastDesyncTick - desyncTickCount;
                if(skipped > 1){
                    for(int i = 1; i < skipped; i++){
                        this.runDesync(player, lastDesyncTick - i);
                    }
                }
                if(skipped > 0){
                    lastDesyncTickRun.put(player, desyncTickCount);
                    this.runDesync(player, desyncTickCount);
                }
            }
        }

        if(tickCount == 0){
            task.run();
            cancel();
        }

        tickCount--;
    }

    public void runDesync(Player player, int tickCount){}

    public void removePlayer(Player player){
        players.remove(player);
    }

}
