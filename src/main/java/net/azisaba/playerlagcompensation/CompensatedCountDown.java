package net.azisaba.playerlagcompensation;

import org.bukkit.entity.Player;

import java.util.List;

public class CompensatedCountDown extends CompensatedTask{

    public CompensatedCountDown(int tickCount, List<Player> players, Runnable task) {
        super(tickCount, players, task);
    }

    @Override
    public void runDesync(Player player, int tickCount){
        //player.sendMessage("tickCount: " + this.tickCount + ", desyncTickCount: " + tickCount);
        if(tickCount >= 0 && tickCount % 20 == 0){
            if(tickCount == 0){
                player.sendTitle("§e§lGO!" ,"" , 0, 20, 0);
            }else {
                //player.sendMessage("§e§l" + (tickCount / 20));
                player.sendTitle("§e§l" + (tickCount / 20),"" , 0, 20, 0);
            }
        }
    }

}
