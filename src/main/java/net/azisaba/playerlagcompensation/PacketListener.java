package net.azisaba.playerlagcompensation;

import ac.grim.grimac.api.GrimUser;
import ac.grim.grimac.player.GrimPlayer;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import net.minecraft.server.v1_12_R1.EntityTracker;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;

public class PacketListener extends PacketAdapter implements Listener {

    PacketListener() {
        super(PlayerLagCompensation.INSTANCE, PacketType.Play.Server.REL_ENTITY_MOVE, PacketType.Play.Server.REL_ENTITY_MOVE_LOOK);
    }

    @Override
    public void onPacketSending(PacketEvent e){
        /*
        Entity pl = e.getPacket().getEntityModifier(e).read(0);
        if(pl instanceof Player && pl != e.getPlayer()){
            GrimPlayer sentUser = PlayerLagCompensation.INSTANCE.getGrimPlayer((Player) pl);
            GrimPlayer receiveTargetUser = PlayerLagCompensation.INSTANCE.getGrimPlayer(e.getPlayer());
            int sentPing = ((Player)pl).spigot().getPing();
            if(sentUser != null){
                sentPing = sentUser.getTransactionPing();
            }
            int receivePing = e.getPlayer().spigot().getPing();
            if(receiveTargetUser != null){
                receivePing = receiveTargetUser.getTransactionPing();
            }
            int delay = (sentPing + receivePing) / 2;
            int delayTicks = delay / 50;
            CompensationPlayer cp = CompensationPlayer.getCompensationPlayer((Player) pl);
            double newX = (Integer) e.getPacket().getModifier().read(1) / 4096D;
            double newY = (Integer) e.getPacket().getModifier().read(2) / 4096D;
            double newZ = (Integer) e.getPacket().getModifier().read(3) / 4096D;
            Location newLoc = pl.getLocation();//.subtract(new Vector(newX, newY, newZ));
            cp.updateLocation(newLoc, e.getPacket().getBooleans().read(0));
            //if(delayTicks < 1){
            //    cp.lastSentLocationMap.put(e.getPlayer().getUniqueId(), newLoc);
            //    return;
            //}
            Location predictedLoc = cp.predictLocation(delayTicks);
            Location lastPredictedLoc = cp.getLastPredictedLocation(e.getPlayer().getUniqueId());
            Location rel = predictedLoc.clone().subtract(lastPredictedLoc);
            if(!cp.lastGround && cp.ground){
                predictedLoc.setY(lastPredictedLoc.getY());
            }
            if(rel.lengthSquared() > 512){
                cp.lastSentLocationMap.put(e.getPlayer().getUniqueId(), newLoc);
                return;
            }
            //e.getPlayer().sendMessage("delayTicks: " + delayTicks + ", realY: " + newLoc.getY() + ", predictedY: " + predictedLoc.getY() + ", ground:" + cp.ground + ", aY: " + cp.a.getY() + ", preRelY: " + rel.getY());
            PacketContainer packet = e.getPacket().deepClone();
            packet.getModifier().write(1, (short) (predictedLoc.getX() * 32 - lastPredictedLoc.getX() * 32) * 128);
            packet.getModifier().write(2, (short) (predictedLoc.getY() * 32 - lastPredictedLoc.getY() * 32) * 128);
            packet.getModifier().write(3, (short) (predictedLoc.getZ() * 32 - lastPredictedLoc.getZ() * 32) * 128);
            e.setPacket(packet);
            if(!cp.lastGround && cp.ground) {
                predictedLoc.add(0, rel.getY(), 0);
            }
            cp.lastSentLocationMap.put(e.getPlayer().getUniqueId(), predictedLoc);
        }

         */
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e){
        ((CraftPlayer)e.getPlayer()).getHandle().impulse = true;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        CompensationPlayer.getCompensationPlayer(e.getPlayer()).removePlayer();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent e){
        CompensationPlayer.getCompensationPlayer(e.getPlayer()).teleport(e.getTo().toVector());
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent e){
        CompensationPlayer.getCompensationPlayer(e.getPlayer()).teleport(e.getPlayer().getLocation().toVector());
    }

}
