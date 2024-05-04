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
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
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

                    PredictionResult result = doPrediction(e, player, packet.isOnGround());

                    Location predictedLoc = result.position;
                    boolean ground = result.onGround;
                    Location rel = result.v;

                    //we don't send a rel move packet if the player is moving too far
                    if (rel.lengthSquared() > 512) {
                        Location loc = player.getLocation();
                        this.sendMoveAsTeleport(e.getPlayer(), packet.getEntityId(), loc.set(predictedLoc.getX(), predictedLoc.getY(), predictedLoc.getZ()), player.isOnGround());
                        result.entry.updateSentLocation(predictedLoc);
                        return;
                    }else if(rel.lengthSquared() == 0){
                        result.entry.updateSentLocation(predictedLoc);
                        return;
                    }

                    WrapperPlayServerEntityRelativeMove nPacket = new WrapperPlayServerEntityRelativeMove(packet.getEntityId(), rel.getX(), rel.getY(), rel.getZ(), ground);
                    e.setCancelled(true);
                    PacketEvents.getAPI().getPlayerManager().sendPacketSilently(e.getPlayer(), nPacket);

                    packet.setDeltaX(rel.getX());
                    packet.setDeltaY(rel.getY());
                    packet.setDeltaZ(rel.getZ());

                    //e.markForReEncode(true);
                    result.entry.updateSentLocation(predictedLoc);
                }
            } else if (e.getPacketType() == PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {
                WrapperPlayServerEntityRelativeMoveAndRotation packet = new WrapperPlayServerEntityRelativeMoveAndRotation(e);
                Player player = getPlayerById(packet.getEntityId());
                if (packet.getEntityId() != e.getUser().getEntityId() && player != null) {

                    PredictionResult result = doPrediction(e, player, packet.isOnGround());

                    Location predictedLoc = result.position;
                    boolean ground = result.onGround;
                    Location rel = result.v;

                    //we don't send a rel move packet if the player is moving too far
                    if (rel.lengthSquared() > 512) {
                        Location loc = player.getLocation().clone();
                        this.sendMoveAsTeleport(e.getPlayer(), packet.getEntityId(), loc.set(predictedLoc.getX(), predictedLoc.getY(), predictedLoc.getZ()), player.isOnGround());
                        result.entry.updateSentLocation(predictedLoc);
                        return;
                    }else if(rel.lengthSquared() == 0){
                        result.entry.updateSentLocation(predictedLoc);
                        return;
                    }

                    packet.setDeltaX(rel.getX());
                    packet.setDeltaY(rel.getY());
                    packet.setDeltaZ(rel.getZ());
                    WrapperPlayServerEntityRelativeMoveAndRotation nPacket = new WrapperPlayServerEntityRelativeMoveAndRotation(packet.getEntityId(), rel.getX(), rel.getY(), rel.getZ(), packet.getYaw(), packet.getPitch(), ground);
                    e.setCancelled(true);
                    PacketEvents.getAPI().getPlayerManager().sendPacketSilently(e.getPlayer(), nPacket);
                    //e.markForReEncode(true);
                    result.entry.updateSentLocation(predictedLoc);
                }
            }else if(e.getPacketType() == PacketType.Play.Server.ENTITY_ROTATION){
                WrapperPlayServerEntityRotation packet = new WrapperPlayServerEntityRotation(e);
                Player player = getPlayerById(packet.getEntityId());
                if (packet.getEntityId() != e.getUser().getEntityId() && player != null) {

                    PredictionResult result = doPrediction(e, player, packet.isOnGround());

                    Location predictedLoc = result.position;
                    boolean ground = result.onGround;
                    Location rel = result.v;

                    //we don't send a rel move packet if the player is moving too far
                    if (rel.lengthSquared() > 512) {
                        Location loc = player.getLocation();
                        this.sendMoveAsTeleport(e.getPlayer(), packet.getEntityId(), loc.set(predictedLoc.getX(), predictedLoc.getY(), predictedLoc.getZ()), player.isOnGround());
                        result.entry.updateSentLocation(predictedLoc);
                        return;
                    }

                    WrapperPlayServerEntityRelativeMoveAndRotation nPacket = new WrapperPlayServerEntityRelativeMoveAndRotation(packet.getEntityId(), rel.getX(), rel.getY(), rel.getZ(), packet.getYaw(), packet.getPitch(), ground);
                    e.setCancelled(true);
                    PacketEvents.getAPI().getPlayerManager().sendPacketSilently(e.getPlayer(), nPacket);
                    //e.markForReEncode(true);
                    result.entry.updateSentLocation(predictedLoc);
                }
            } else if (e.getPacketType() == PacketType.Play.Server.ENTITY_TELEPORT) {
                WrapperPlayServerEntityTeleport packet = new WrapperPlayServerEntityTeleport(e);
                Player player = getPlayerById(packet.getEntityId());
                if (packet.getEntityId() != e.getUser().getEntityId() && player != null) {

                    PredictionResult result = doPrediction(e, player, packet.isOnGround());

                    Location predictedLoc = result.position;
                    boolean ground = result.onGround;
                    Location rel = result.v;

                    if(rel.lengthSquared() == 0){
                        result.entry.updateSentLocation(predictedLoc);
                        return;
                    }

                    packet.setPosition(new Vector3d(predictedLoc.getX(), predictedLoc.getY(), predictedLoc.getZ()));
                    WrapperPlayServerEntityTeleport nPacket = new WrapperPlayServerEntityTeleport(packet.getEntityId(), new Vector3d(predictedLoc.getX(), predictedLoc.getY(), predictedLoc.getZ()), packet.getYaw(), packet.getPitch(), ground);
                    e.setCancelled(true);
                    PacketEvents.getAPI().getPlayerManager().sendPacketSilently(e.getPlayer(), nPacket);
                    //e.markForReEncode(true);
                    result.entry.updateSentLocation(predictedLoc);
                }
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public PredictionResult doPrediction(PacketSendEvent e, Player player, boolean onGround){
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
        CompensationPlayer.CompensationPlayerEntry entry = cp.entryMap.get(receive.getUniqueId());
        if(entry == null){
            entry = (cp).new CompensationPlayerEntry(cp.uuid, cp);
            cp.entryMap.put(receive.getUniqueId(), entry);
        }
        Location newLoc = player.getLocation().clone();
        cp.updateLocation(newLoc, player.isOnGround());
        if(delayTicks < 1){
            return new PredictionResult(newLoc, onGround, entry, newLoc.clone().zero());
        }

        //predict movement
        Pair<Location, Boolean> preResult = cp.predictLocation(delayTicks);
        Location predictedLoc = preResult.getFirst();
        boolean preGround = preResult.getSecond();
        Location lastPredictedLoc = entry.lastSentLocation.clone();
        if (cp.lastRelMove.lengthSquared() <= 0.03) {
            predictedLoc = newLoc;
        }
        Location rel = predictedLoc.clone().subtract(lastPredictedLoc);

        //virtual spring force
        Vector calPreV = rel.toVector().multiply(this.getVirtualSpringConstant(preGround));

        //virtual damper force
        Vector damperA = entry.getPredictionAcceleration(predictedLoc).toVector().multiply(this.getVirtualDamperConstant(preGround));

        //calculate new predicted location
        predictedLoc = lastPredictedLoc.clone().add(calPreV.subtract(damperA));

        receive.sendMessage(/*"delayTicks: " + delayTicks + ", preX: " + this.round(predictedLoc.getX()) + */", preVX: " + this.round(rel.getX()) + ", calPreVX: " + this.round(calPreV.getX()) + ", damperAX: " + this.round(damperA.getX()));

        return new PredictionResult(predictedLoc, preGround, entry, predictedLoc.clone().set(calPreV.getX(), calPreV.getY(), calPreV.getZ()));
    }

    public Vector getVirtualSpringConstant(boolean preGround){
        if(preGround){
            return new Vector(1.1, 1.05, 1.1);
        }else {
            return new Vector(1.1, 1.1, 1.1);
        }
    }

    public Vector getVirtualDamperConstant(boolean preGround){
        if(preGround){
            return new Vector(0.2, 0.1, 0.2);
        }else {
            return new Vector(0.2, 0.2, 0.2);
        }
    }

    public void sendMoveAsTeleport(Object player, int entityId, Location loc, boolean ground){
        WrapperPlayServerEntityTeleport packet = new WrapperPlayServerEntityTeleport(entityId, new Vector3d(loc.getX(), loc.getY(), loc.getZ()), loc.getYaw(), loc.getPitch(), ground);
        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);

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
