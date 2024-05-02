package net.azisaba.playerlagcompensation;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketListenerAbstract;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketListenerPriority;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3d;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import ac.grim.grimac.utils.data.Pair;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PacketEventListener extends PacketListenerAbstract {

    public PacketEventListener() {
        super(PacketListenerPriority.LOWEST);
    }

    @Override
    public void onPacketSend(PacketSendEvent e) {
        try {
            if (e.isCancelled()) return;

            if (e.getPacketType() == PacketType.Play.Server.ENTITY_RELATIVE_MOVE) {
                WrapperPlayServerEntityRelativeMove packet = new WrapperPlayServerEntityRelativeMove(e);
                Player player = getPlayerById(packet.getEntityId());
                if (packet.getEntityId() != e.getUser().getEntityId() && player != null) {
                    GrimPlayer sentUser = PlayerLagCompensation.INSTANCE.getGrimPlayer(player);
                    Player receive = this.getPlayerById(e.getUser().getEntityId());
                    GrimPlayer receiveTargetUser = PlayerLagCompensation.INSTANCE.getGrimPlayer(receive);
                    int sentPing = player.spigot().getPing();
                    if (sentUser != null) {
                        sentPing = sentUser.getTransactionPing();
                    }
                    int receivePing = receive.spigot().getPing();
                    if (receiveTargetUser != null) {
                        receivePing = receiveTargetUser.getTransactionPing();
                    }
                    int delay = (sentPing + receivePing) / 2;
                    int delayTicks = delay / 50;
                    CompensationPlayer cp = CompensationPlayer.getCompensationPlayer(player);
                    double newX = packet.getDeltaX();
                    double newY = packet.getDeltaY();
                    double newZ = packet.getDeltaZ();
                    Vector newLoc = player.getLocation().toVector();//cp.lastVector.add(new Vector(newX, newY, newZ));
                    cp.updateLocation(newLoc, player.isOnGround());
                    //if(delayTicks < 1){
                    //    cp.lastSentLocationMap.put(receive.getUniqueId(), newLoc);
                    //    return;
                    //}
                    Pair<Vector, Boolean> preResult = cp.predictLocation(delayTicks);
                    Vector predictedLoc = preResult.getFirst();
                    boolean ground = preResult.getSecond();
                    Vector lastPredictedLoc = cp.getLastPredictedLocation(receive.getUniqueId());
                    if (cp.lastRelMove.lengthSquared() <= 0.03) {
                        predictedLoc = newLoc;
                    }
                    Vector rel = predictedLoc.clone().subtract(lastPredictedLoc);
                    //Vector preA = cp.getPredictionAcceleration(receive.getUniqueId(), predictedLoc, lastPredictedLoc);
                    //if(rel.lengthSquared() > 0.03) {
                        //preA.multiply(0.8);
                    //    preA.setX(preA.getX() * 0.8);
                    //    if(preA.getY() > 0) {
                    //        preA.setY(preA.getY() * 0.8);
                    //    }
                    //    preA.setZ(preA.getZ() * 0.8);
                    //}
                    //Vector calculatedPreV = cp.getLastPredictionVelocity(receive.getUniqueId(), lastPredictedLoc).clone().add(preA);
                    //predictedLoc = lastPredictedLoc.clone().add(calculatedPreV);
                    if (rel.lengthSquared() > 512) {
                        cp.lastLastSentVectorMap.put(receive.getUniqueId(), lastPredictedLoc);
                        cp.lastSentVectorMap.put(receive.getUniqueId(), newLoc);
                        return;
                    }
                    //receive.sendMessage("delayTicks: " + delayTicks + ", realY: " + this.round(newLoc.getY()) + ", predictedY: " + this.round(predictedLoc.getY()) + ", ground:" + ground);

                    WrapperPlayServerEntityRelativeMove nPacket = new WrapperPlayServerEntityRelativeMove(packet.getEntityId(), rel.getX(), rel.getY(), rel.getZ(), ground);
                    e.setCancelled(true);
                    PacketEvents.getAPI().getPlayerManager().sendPacketSilently(e.getPlayer(), nPacket);

                    packet.setDeltaX(rel.getX());
                    packet.setDeltaY(rel.getY());
                    packet.setDeltaZ(rel.getZ());
                    //e.markForReEncode(true);
                    cp.lastLastSentVectorMap.put(receive.getUniqueId(), lastPredictedLoc);
                    cp.lastSentVectorMap.put(receive.getUniqueId(), predictedLoc);
                }
            } else if (e.getPacketType() == PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {
                WrapperPlayServerEntityRelativeMoveAndRotation packet = new WrapperPlayServerEntityRelativeMoveAndRotation(e);
                Player player = getPlayerById(packet.getEntityId());
                if (packet.getEntityId() != e.getUser().getEntityId() && player != null) {
                    GrimPlayer sentUser = PlayerLagCompensation.INSTANCE.getGrimPlayer(player);
                    Player receive = this.getPlayerById(e.getUser().getEntityId());
                    GrimPlayer receiveTargetUser = PlayerLagCompensation.INSTANCE.getGrimPlayer(receive);
                    int sentPing = player.spigot().getPing();
                    if (sentUser != null) {
                        sentPing = sentUser.getTransactionPing();
                    }
                    int receivePing = receive.spigot().getPing();
                    if (receiveTargetUser != null) {
                        receivePing = receiveTargetUser.getTransactionPing();
                    }
                    int delay = (sentPing + receivePing) / 2;
                    int delayTicks = delay / 50;
                    CompensationPlayer cp = CompensationPlayer.getCompensationPlayer(player);
                    double newX = packet.getDeltaX();
                    double newY = packet.getDeltaY();
                    double newZ = packet.getDeltaZ();
                    Vector newLoc = player.getLocation().toVector();//cp.lastVector.add(new Vector(newX, newY, newZ));
                    cp.updateLocation(newLoc, packet.isOnGround());
                    //if(delayTicks < 1){
                    //    cp.lastSentLocationMap.put(receive.getUniqueId(), newLoc);
                    //    return;
                    //}
                    Pair<Vector, Boolean> preResult = cp.predictLocation(delayTicks);
                    Vector predictedLoc = preResult.getFirst();
                    boolean ground = preResult.getSecond();
                    Vector lastPredictedLoc = cp.getLastPredictedLocation(receive.getUniqueId());
                    if (cp.lastRelMove.lengthSquared() <= 0.03) {
                        predictedLoc = newLoc;
                    }
                    Vector rel = predictedLoc.clone().subtract(lastPredictedLoc);
                    //Vector preA = cp.getPredictionAcceleration(receive.getUniqueId(), predictedLoc, lastPredictedLoc);
                    /*if(rel.lengthSquared() > 0.03) {
                        //preA.multiply(0.8);
                        preA.setX(preA.getX() * 0.8);
                        if(preA.getY() > 0) {
                            preA.setY(preA.getY() * 0.8);
                        }
                        preA.setZ(preA.getZ() * 0.8);
                    }*/
                    //Vector calculatedPreV = cp.getLastPredictionVelocity(receive.getUniqueId(), lastPredictedLoc).clone().add(preA);
                    //predictedLoc = lastPredictedLoc.clone().add(calculatedPreV);

                    if (rel.lengthSquared() > 512) {
                        cp.lastLastSentVectorMap.put(receive.getUniqueId(), lastPredictedLoc);
                        cp.lastSentVectorMap.put(receive.getUniqueId(), newLoc);
                        return;
                    }
                    //receive.sendMessage("delayTicks: " + delayTicks + ", realY: " + this.round(newLoc.getY()) + ", predictedY: " + this.round(predictedLoc.getY()) + ", ground:" + ground);
                    packet.setDeltaX(rel.getX());
                    packet.setDeltaY(rel.getY());
                    packet.setDeltaZ(rel.getZ());
                    WrapperPlayServerEntityRelativeMoveAndRotation nPacket = new WrapperPlayServerEntityRelativeMoveAndRotation(packet.getEntityId(), rel.getX(), rel.getY(), rel.getZ(), packet.getYaw(), packet.getPitch(), ground);
                    e.setCancelled(true);
                    PacketEvents.getAPI().getPlayerManager().sendPacketSilently(e.getPlayer(), nPacket);
                    //e.markForReEncode(true);
                    cp.lastLastSentVectorMap.put(receive.getUniqueId(), lastPredictedLoc);
                    cp.lastSentVectorMap.put(receive.getUniqueId(), predictedLoc);
                }
            } else if (e.getPacketType() == PacketType.Play.Server.ENTITY_TELEPORT) {
                WrapperPlayServerEntityTeleport packet = new WrapperPlayServerEntityTeleport(e);
                Player player = getPlayerById(packet.getEntityId());
                if (packet.getEntityId() != e.getUser().getEntityId() && player != null) {
                    GrimPlayer sentUser = PlayerLagCompensation.INSTANCE.getGrimPlayer(player);
                    Player receive = this.getPlayerById(e.getUser().getEntityId());
                    GrimPlayer receiveTargetUser = PlayerLagCompensation.INSTANCE.getGrimPlayer(receive);
                    int sentPing = player.spigot().getPing();
                    if (sentUser != null) {
                        sentPing = sentUser.getTransactionPing();
                    }
                    int receivePing = receive.spigot().getPing();
                    if (receiveTargetUser != null) {
                        receivePing = receiveTargetUser.getTransactionPing();
                    }
                    int delay = (sentPing + receivePing) / 2;
                    int delayTicks = delay / 50;
                    CompensationPlayer cp = CompensationPlayer.getCompensationPlayer(player);
                    Vector newLoc = player.getLocation().toVector();//cp.lastVector.add(new Vector(newX, newY, newZ));
                    cp.updateLocation(newLoc, packet.isOnGround());
                    //if(delayTicks < 1){
                    //    cp.lastSentLocationMap.put(receive.getUniqueId(), newLoc);
                    //    return;
                    //}
                    Pair<Vector, Boolean> preResult = cp.predictLocation(delayTicks);
                    Vector predictedLoc = preResult.getFirst();
                    boolean ground = preResult.getSecond();
                    Vector lastPredictedLoc = cp.getLastPredictedLocation(receive.getUniqueId());
                    if (cp.lastRelMove.lengthSquared() <= 0.03) {
                        predictedLoc = newLoc;
                    }
                    Vector rel = predictedLoc.clone().subtract(lastPredictedLoc);
                    //Vector preA = cp.getPredictionAcceleration(receive.getUniqueId(), predictedLoc, lastPredictedLoc);
                    /*if(rel.lengthSquared() > 0.03) {
                        //preA.multiply(0.8);
                        preA.setX(preA.getX() * 0.8);
                        if(preA.getY() > 0) {
                            preA.setY(preA.getY() * 0.8);
                        }
                        preA.setZ(preA.getZ() * 0.8);
                    }*/
                    //Vector calculatedPreV = cp.getLastPredictionVelocity(receive.getUniqueId(), lastPredictedLoc).clone().add(preA);
                    //predictedLoc = lastPredictedLoc.clone().add(calculatedPreV);

                    if (rel.lengthSquared() > 512) {
                        cp.lastLastSentVectorMap.put(receive.getUniqueId(), lastPredictedLoc);
                        cp.lastSentVectorMap.put(receive.getUniqueId(), newLoc);
                        return;
                    }
                    //receive.sendMessage("delayTicks: " + delayTicks + ", realY: " + this.round(newLoc.getY()) + ", predictedY: " + this.round(predictedLoc.getY()) + ", ground:" + ground);
                    packet.setPosition(new Vector3d(predictedLoc.getX(), predictedLoc.getY(), predictedLoc.getZ()));
                    WrapperPlayServerEntityTeleport nPacket = new WrapperPlayServerEntityTeleport(packet.getEntityId(), new Vector3d(predictedLoc.getX(), predictedLoc.getY(), predictedLoc.getZ()), packet.getYaw(), packet.getPitch(), ground);
                    e.setCancelled(true);
                    PacketEvents.getAPI().getPlayerManager().sendPacketSilently(e.getPlayer(), nPacket);
                    //e.markForReEncode(true);
                    cp.lastLastSentVectorMap.put(receive.getUniqueId(), lastPredictedLoc);
                    cp.lastSentVectorMap.put(receive.getUniqueId(), predictedLoc);
                }
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Player getPlayerById(int id){
        for(Player player : Bukkit.getOnlinePlayers()){
            if(player.getEntityId() == id){
                return player;
            }
        }
        return null;
    }

    private double round(double value){
        return Math.round(value * 1000) / 1000.0;
    }

}
