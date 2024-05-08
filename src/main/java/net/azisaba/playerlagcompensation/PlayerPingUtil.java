package net.azisaba.playerlagcompensation;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PlayerPingUtil {

    public static HashMap<UUID, ConcurrentLinkedQueue<Integer>> playerPingMap = new HashMap<>();

    public static void addPing(UUID uuid, int ping){
        if(!playerPingMap.containsKey(uuid)){
            playerPingMap.put(uuid, new ConcurrentLinkedQueue<>());
        }
        ConcurrentLinkedQueue<Integer> pings = playerPingMap.get(uuid);
        pings.add(ping);
        if(pings.size() > 10){
            pings.poll();
        }
    }

    public static int getPing(UUID uuid){
        if(!playerPingMap.containsKey(uuid)){
            return Bukkit.getPlayer(uuid).spigot().getPing();
        }
        ConcurrentLinkedQueue<Integer> pings = playerPingMap.get(uuid);
        int sum = 0;
        for(int ping : pings){
            sum += ping;
        }
        return sum / pings.size();
    }

    public static boolean isPingStable(UUID uuid) {
        if (!playerPingMap.containsKey(uuid)) {
            return false;
        }
        ConcurrentLinkedQueue<Integer> pings = playerPingMap.get(uuid);
        return pings.size() == 10;
    }

    public static boolean isAllPlayersPingStable(List<Player> players){
        for(Player player : players){
            if(!isPingStable(player.getUniqueId())){
                return false;
            }
        }
        return true;
    }

    public static int getMostHighestPing(List<Player> players){
        int highest = 0;
        for(Player player : players){
            int ping = getPing(player.getUniqueId());
            if(ping > highest){
                highest = ping;
            }
        }
        return highest;
    }

    public static void removePlayer(UUID uuid) {
        playerPingMap.remove(uuid);
    }

}
