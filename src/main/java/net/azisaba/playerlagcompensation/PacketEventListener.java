package net.azisaba.playerlagcompensation;

import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import ac.grim.grimac.utils.data.Pair;
import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PacketEventListener extends PacketListenerAbstract {

    public PacketEventListener() {
        super(PacketListenerPriority.LOWEST);
    }

    @Override
    public void onPacketSend(PacketSendEvent e) {
        try {
            if(!ToggleCompensationCommand.shouldCompensate) return;
            if (e.isCancelled()) return;

            if (e.getPacketType() == PacketType.Play.Server.ENTITY_RELATIVE_MOVE) {
                WrapperPlayServerEntityRelativeMove packet = new WrapperPlayServerEntityRelativeMove(e);
                Player player = getPlayerById(packet.getEntityId());
                if (packet.getEntityId() != e.getUser().getEntityId() && player != null) {

                    PredictionResult result = doPrediction(e, player, packet.isOnGround());

                    Location predictedLoc = result.position.clone();
                    boolean ground = result.onGround;
                    Location rel = result.v;

                    //we don't send a rel move packet if the player is moving too far
                    if (rel.getX() > 8 || rel.getY() > 8 || rel.getZ() > 8) {
                        this.sendMoveAsTeleport(e.getPlayer(), packet.getEntityId(), predictedLoc, player.isOnGround());
                        result.entry.updateSentLocation(predictedLoc, result.delayTicks);
                        packet.setDeltaX(rel.getX());
                        packet.setDeltaY(rel.getY());
                        packet.setDeltaZ(rel.getZ());
                        e.setCancelled(true);
                        return;
                    }//else if(rel.lengthSquared() == 0){
                    //    result.entry.updateSentLocation(predictedLoc, result.delayTicks);
                    //    return;
                    //}

                    WrapperPlayServerEntityRelativeMove nPacket = new WrapperPlayServerEntityRelativeMove(packet.getEntityId(), rel.getX(), rel.getY(), rel.getZ(), ground);
                    //e.setCancelled(true);
                    //PacketEvents.getAPI().getPlayerManager().sendPacketSilently(e.getPlayer(), nPacket);

                    packet.setDeltaX(rel.getX());
                    packet.setDeltaY(rel.getY());
                    packet.setDeltaZ(rel.getZ());

                    e.markForReEncode(true);
                    result.entry.updateSentLocation(predictedLoc, result.delayTicks);
                }
            } else if (e.getPacketType() == PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {
                WrapperPlayServerEntityRelativeMoveAndRotation packet = new WrapperPlayServerEntityRelativeMoveAndRotation(e);
                Player player = getPlayerById(packet.getEntityId());
                if (packet.getEntityId() != e.getUser().getEntityId() && player != null) {

                    PredictionResult result = doPrediction(e, player, packet.isOnGround());

                    Location predictedLoc = result.position.clone();
                    boolean ground = result.onGround;
                    Location rel = result.v;

                    //we don't send a rel move packet if the player is moving too far
                    if (rel.getX() > 8 || rel.getY() > 8 || rel.getZ() > 8) {
                        this.sendMoveAsTeleport(e.getPlayer(), packet.getEntityId(), predictedLoc, player.isOnGround());
                        result.entry.updateSentLocation(predictedLoc, result.delayTicks);
                        packet.setDeltaX(rel.getX());
                        packet.setDeltaY(rel.getY());
                        packet.setDeltaZ(rel.getZ());
                        e.setCancelled(true);
                        return;
                    }//else if(rel.lengthSquared() == 0){
                    //    result.entry.updateSentLocation(predictedLoc, result.delayTicks);
                    //    return;
                    //}

                    packet.setDeltaX(rel.getX());
                    packet.setDeltaY(rel.getY());
                    packet.setDeltaZ(rel.getZ());
                    WrapperPlayServerEntityRelativeMoveAndRotation nPacket = new WrapperPlayServerEntityRelativeMoveAndRotation(packet.getEntityId(), rel.getX(), rel.getY(), rel.getZ(), packet.getYaw(), packet.getPitch(), ground);
                    //e.setCancelled(true);
                    //PacketEvents.getAPI().getPlayerManager().sendPacketSilently(e.getPlayer(), nPacket);
                    e.markForReEncode(true);
                    result.entry.updateSentLocation(predictedLoc, result.delayTicks);
                }
            }else if(e.getPacketType() == PacketType.Play.Server.ENTITY_ROTATION){
                WrapperPlayServerEntityRotation packet = new WrapperPlayServerEntityRotation(e);
                Player player = getPlayerById(packet.getEntityId());
                if (packet.getEntityId() != e.getUser().getEntityId() && player != null) {

                    PredictionResult result = doPrediction(e, player, packet.isOnGround());

                    Location predictedLoc = result.position.clone();
                    boolean ground = result.onGround;
                    Location rel = result.v;

                    //we don't send a rel move packet if the player is moving too far
                    if (rel.getX() > 8 || rel.getY() > 8 || rel.getZ() > 8) {
                        //Location loc = player.getLocation();
                        this.sendMoveAsTeleport(e.getPlayer(), packet.getEntityId(), predictedLoc, player.isOnGround());
                        result.entry.updateSentLocation(predictedLoc, result.delayTicks);
                        e.setCancelled(true);
                        return;
                    }

                    WrapperPlayServerEntityRelativeMoveAndRotation nPacket = new WrapperPlayServerEntityRelativeMoveAndRotation(packet.getEntityId(), rel.getX(), rel.getY(), rel.getZ(), packet.getYaw(), packet.getPitch(), ground);
                    e.setCancelled(true);
                    PacketEvents.getAPI().getPlayerManager().sendPacket(e.getPlayer(), nPacket);
                    //e.markForReEncode(true);
                    result.entry.updateSentLocation(predictedLoc, result.delayTicks);
                }
            } else if (e.getPacketType() == PacketType.Play.Server.ENTITY_TELEPORT) {
                WrapperPlayServerEntityTeleport packet = new WrapperPlayServerEntityTeleport(e);
                Player player = getPlayerById(packet.getEntityId());
                if (packet.getEntityId() != e.getUser().getEntityId() && player != null) {

                    PredictionResult result = doPrediction(e, player, packet.isOnGround());

                    Location predictedLoc = result.position.clone();
                    boolean ground = result.onGround;
                    Location rel = result.v;

                    //if(rel.lengthSquared() == 0){
                    //    result.entry.updateSentLocation(predictedLoc, result.delayTicks);
                    //    return;
                    //}

                    packet.setPosition(new Vector3d(predictedLoc.getX(), predictedLoc.getY(), predictedLoc.getZ()));
                    WrapperPlayServerEntityTeleport nPacket = new WrapperPlayServerEntityTeleport(packet.getEntityId(), new Vector3d(predictedLoc.getX(), predictedLoc.getY(), predictedLoc.getZ()), packet.getYaw(), packet.getPitch(), ground);
                    //e.setCancelled(true);
                    //PacketEvents.getAPI().getPlayerManager().sendPacketSilently(e.getPlayer(), nPacket);
                    e.markForReEncode(true);
                    result.entry.updateSentLocation(predictedLoc, result.delayTicks);
                }
            }else if(e.getPacketType() == PacketType.Play.Server.ENTITY_VELOCITY){
                if(!ToggleVelocityCompensationCommand.shouldVelocityCompensate) return;

                WrapperPlayServerEntityVelocity packet = new WrapperPlayServerEntityVelocity(e);
                Player player = getPlayerById(packet.getEntityId());
                if(packet.getEntityId() == e.getUser().getEntityId() && player != null){
                    CompensationPlayer cp = CompensationPlayer.getCompensationPlayer(player);
                    int sentPing = 0;//player.spigot().getPing();
                    if (cp.gp != null) {
                        sentPing = cp.gp.getTransactionPing();
                    }
                    int delayTicks = sentPing / 100;//(int) Math.round(sentPing / 100D);
                    cp.lastVelocity = new CompensationVelocityData(new Vector(packet.getVelocity().x, packet.getVelocity().y, packet.getVelocity().z), delayTicks);
                }
                e.markForReEncode(false);
            }else if(e.getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK){
                WrapperPlayServerPlayerPositionAndLook packet = new WrapperPlayServerPlayerPositionAndLook(e);
                if(!packet.isRelativeFlag(RelativeFlag.X) && !packet.isRelativeFlag(RelativeFlag.Y) && !packet.isRelativeFlag(RelativeFlag.Z)){
                    CompensationPlayer.getCompensationPlayer((Player)e.getPlayer()).teleport(new Location(((Player) e.getPlayer()).getWorld() ,packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch()));
                }
                e.markForReEncode(false);
            }else {
                e.markForReEncode(false);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public PredictionResult doPrediction(PacketSendEvent e, Player player, boolean onGround){
        GrimPlayer sentUser = PlayerLagCompensation.INSTANCE.getGrimPlayer(player);
        Player receive = this.getPlayerById(e.getUser().getEntityId());
        GrimPlayer receiveTargetUser = PlayerLagCompensation.INSTANCE.getGrimPlayer(receive);
        int sentPing = 0;//player.spigot().getPing();
        if (sentUser != null) {
            sentPing = sentUser.getTransactionPing();
        }
        int receivePing = 0;//receive.spigot().getPing();
        if (receiveTargetUser != null) {
            receivePing = receiveTargetUser.getTransactionPing();
        }
        int delay = sentPing + receivePing;
        int delayTicks = delay / 100;//(int) Math.round(delay / 100D);

        CompensationPlayer cp = CompensationPlayer.getCompensationPlayer(player);
        CompensationPlayer.CompensationPlayerEntry entry = cp.entryMap.get(receive.getUniqueId());
        if(entry == null){
            entry = (cp).new CompensationPlayerEntry(cp.uuid, cp);
            cp.entryMap.put(receive.getUniqueId(), entry);
        }
        Location newLoc = player.getLocation().clone();
        if (sentUser == null) {
            cp.updateLocation(newLoc, player.isOnGround());
        }

        //predict movement
        Pair<Location, Boolean> preResult = cp.predictLocation(delayTicks);
        Location predictedLoc = preResult.getFirst();
        boolean preGround = preResult.getSecond();
        Location lastPredictedLoc = entry.lastSentLocation.clone();
        //if (cp.lastRelMove.lengthSquared() <= 0.03) {
        //    predictedLoc = newLoc;
        //}
        Location rel = predictedLoc.clone().subtract(lastPredictedLoc);

        //virtual spring force
        Vector calPreV = rel.toVector().multiply(this.getVirtualSpringConstant(onGround, delayTicks, entry.lastDelayTicks, cp.kbDesync));

        //virtual damper force
        Vector damperA = entry.getPredictionAcceleration(predictedLoc).toVector().multiply(this.getVirtualDamperConstant(onGround, delayTicks, entry.lastDelayTicks, cp.kbDesync));

        //calculate new predicted location
        predictedLoc = lastPredictedLoc.clone().add(calPreV.subtract(damperA));

        if(cp.lastVelocity != null){
            //receive.sendMessage("delayTicks: " + cp.lastVelocity.delayTicks + ", veloY: " + cp.lastVelocity.velocity.getY());
        }
        //player.sendMessage("delayTicks: " + delayTicks /*+ ", preY: " + this.round(predictedLoc.getY()) + ", preVY: " + this.round(rel.getY())*/ + ", relY: " + this.round(rel.getY()) + ", calPreVY: " + this.round(calPreV.getY()));

        return new PredictionResult(predictedLoc, preGround, entry, calPreV.clone().toLocation(predictedLoc.getWorld()), delayTicks);
    }

    public Vector getVirtualSpringConstant(boolean onGround, int delayTicks, int lastDelayTicks, boolean kbDesync){

        if(kbDesync){
            return new Vector(1, 1, 1);
        }

        int delayDiff = delayTicks - lastDelayTicks;

        double x = 1.0 + delayTicks * 0.04 + delayDiff * 0.05;
        double y = (onGround ? 1 : 1.05);// + delayDiff * 0.05;
        double z = 1.0 + delayTicks * 0.04 + delayDiff * 0.05;

        return new Vector(x, y, z);
    }

    public Vector getVirtualDamperConstant(boolean onGround, int delayTicks, int lastDelayTicks, boolean kbDesync){

        if(kbDesync){
            return new Vector(0, 0, 0);
        }

        int delayDiff = delayTicks - lastDelayTicks;

        double x = delayTicks * 0.1 + delayDiff * 0.05;
        double y = onGround ? 0 : delayTicks * 0.05 + delayDiff * 0.05;
        double z = delayTicks * 0.1 + delayDiff * 0.05;

        return new Vector(x, y, z);
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
