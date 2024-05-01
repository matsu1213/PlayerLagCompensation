package net.azisaba.playerlagcompensation;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketListenerAbstract;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketListenerPriority;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation;
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
            if(e.isCancelled()) return;

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
                    Vector predictedLoc = cp.predictLocation(delayTicks);
                    Vector lastPredictedLoc = cp.getLastPredictedLocation(receive.getUniqueId());
                    Vector rel = predictedLoc.clone().subtract(lastPredictedLoc);
                    Vector preA = cp.getPredictionAcceleration(receive.getUniqueId(), predictedLoc, lastPredictedLoc);
                    preA.multiply(0.8);
                    Vector calculatedPreV = cp.getLastPredictionVelocity(receive.getUniqueId(), lastPredictedLoc).clone().add(preA);
                    predictedLoc = lastPredictedLoc.clone().add(calculatedPreV);
                    if(cp.lastRelMove.lengthSquared() <= 0.03){
                        predictedLoc = newLoc;
                    }
                    //if (!cp.lastGround && cp.ground) {
                    //    predictedLoc.setY(lastPredictedLoc.getY());
                    //}
                    if (rel.lengthSquared() > 512) {
                        cp.lastLastSentVectorMap.put(receive.getUniqueId(), lastPredictedLoc);
                        cp.lastSentVectorMap.put(receive.getUniqueId(), newLoc);
                        return;
                    }
                    receive.sendMessage("delayTicks: " + delayTicks + ", realX: " + this.round(newLoc.getX()) + ", predictedX: " + this.round(predictedLoc.getX()) + ", ground:" + cp.ground + ", preRelX: " + this.round(rel.getX()));

                    WrapperPlayServerEntityRelativeMove nPacket = new WrapperPlayServerEntityRelativeMove(packet.getEntityId(), rel.getX(), rel.getY(), rel.getZ(), packet.isOnGround());
                    e.setCancelled(true);
                    PacketEvents.getAPI().getPlayerManager().sendPacketSilently(e.getPlayer(), nPacket);

                    packet.setDeltaX(rel.getX());
                    packet.setDeltaY(rel.getY());
                    packet.setDeltaZ(rel.getZ());

                    //e.markForReEncode(true);
                    cp.lastLastSentVectorMap.put(receive.getUniqueId(), lastPredictedLoc);
                    cp.lastSentVectorMap.put(receive.getUniqueId(), predictedLoc);
                }
            }else if(e.getPacketType() == PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {
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
                    Vector predictedLoc = cp.predictLocation(delayTicks);
                    Vector lastPredictedLoc = cp.getLastPredictedLocation(receive.getUniqueId());
                    Vector rel = predictedLoc.clone().subtract(lastPredictedLoc);
                    Vector preA = cp.getPredictionAcceleration(receive.getUniqueId(), predictedLoc, lastPredictedLoc);
                    preA.multiply(0.8);
                    Vector calculatedPreV = cp.getLastPredictionVelocity(receive.getUniqueId(), lastPredictedLoc).clone().add(preA);
                    predictedLoc = lastPredictedLoc.clone().add(calculatedPreV);
                    if(cp.lastRelMove.lengthSquared() <= 0.03){
                        predictedLoc = newLoc;
                    }
                    //if (!cp.lastGround && cp.ground) {
                    //    predictedLoc.setY(lastPredictedLoc.getY());
                    //}
                    if (rel.lengthSquared() > 512) {
                        cp.lastLastSentVectorMap.put(receive.getUniqueId(), lastPredictedLoc);
                        cp.lastSentVectorMap.put(receive.getUniqueId(), newLoc);
                        return;
                    }
                    receive.sendMessage("delayTicks: " + delayTicks + ", realX: " + this.round(newLoc.getX()) + ", predictedX: " + this.round(predictedLoc.getX()) + ", ground:" + cp.ground + ", preRelX: " + this.round(rel.getX()));
                    packet.setDeltaX(rel.getX());
                    packet.setDeltaY(rel.getY());
                    packet.setDeltaZ(rel.getZ());
                    WrapperPlayServerEntityRelativeMoveAndRotation nPacket = new WrapperPlayServerEntityRelativeMoveAndRotation(packet.getEntityId(), rel.getX(), rel.getY(), rel.getZ(), packet.getYaw(), packet.getPitch(), packet.isOnGround());
                    e.setCancelled(true);
                    PacketEvents.getAPI().getPlayerManager().sendPacketSilently(e.getPlayer(), nPacket);
                    //packet.write();
                    //e.markForReEncode(true);
                    cp.lastLastSentVectorMap.put(receive.getUniqueId(), lastPredictedLoc);
                    cp.lastSentVectorMap.put(receive.getUniqueId(), predictedLoc);
                }
            }
        } catch (Exception ex) {
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
