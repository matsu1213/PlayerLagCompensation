package net.azisaba.playerlagcompensation;

import ac.grim.grimac.api.events.CompletePredictionEvent;
import ac.grim.grimac.player.GrimPlayer;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

public class BukkitEventListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e){
        //((CraftPlayer)e.getPlayer()).getHandle().impulse = true;
        //e.getPlayer().getWorld().spawnParticle(Particle.REDSTONE, e.getTo(), 0, 0.0001, 0.0, 255.0 /255, 1.0);
    }

    @EventHandler
    public void onPredictionComplete(CompletePredictionEvent e){
        GrimPlayer gp = (GrimPlayer) e.getPlayer();
        CompensationPlayer.getCompensationPlayer((gp.bukkitPlayer)).updateLocation(new Location(gp.bukkitPlayer.getWorld(), gp.x, gp.y, gp.z), gp.onGround);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        PlayerPingUtil.removePlayer(e.getPlayer().getUniqueId());
        CompensationPlayer.getCompensationPlayer(e.getPlayer()).removePlayer();
    }

    //@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    //public void onTeleport(PlayerTeleportEvent e){
    //    CompensationPlayer.getCompensationPlayer(e.getPlayer()).teleport(e.getTo());
    //}

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent e){
        CompensationPlayer cp = CompensationPlayer.getCompensationPlayer(e.getPlayer());
        cp.teleport(e.getPlayer().getLocation());
        cp.entryMap.clear();
    }

}
