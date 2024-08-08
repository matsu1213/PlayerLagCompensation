package net.azisaba.playerlagcompensation;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.data.Pair;
import ac.grim.grimac.utils.nmsutil.Collisions;
import ac.grim.grimac.utils.nmsutil.GetBoundingBox;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public class CompensationPlayer {

    static HashMap<UUID, CompensationPlayer> playerSet = new HashMap<>();
    static CompensationPlayer getCompensationPlayer(Player player){
        if(playerSet.containsKey(player.getUniqueId())) {
            return playerSet.get(player.getUniqueId());
        } else {
            CompensationPlayer cp = new CompensationPlayer(player);
            playerSet.put(player.getUniqueId(), cp);
            return cp;
        }
    }

    public UUID uuid;
    public GrimPlayer gp;
    public Location lastLocation;
    public Location lastLastLocation;
    public Location lastLastLastLocation;
    public Location lastRelMove;
    public Location lastLastRelMove;
    public Location v;
    public Location a;
    public boolean ground = false;
    public boolean lastGround = false;
    public boolean sprinting = false;
    public boolean lastLastGround = false;
    public boolean xCollide = false;
    public boolean zCollide = false;

    public boolean kbDesync = false;
    public Location desyncLastLoc;
    public Location desyncLoc;
    public Location desyncV;
    public Location desyncA;
    public boolean desyncGround = false;

    public CompensationVelocityData lastVelocity = null;

    public HashMap<UUID, CompensationPlayerEntry> entryMap = new HashMap<>();
    public HashMap<Integer, Pair<Location,Boolean>> calculatedLocations = new HashMap<>();

    CompensationPlayer(Player player){
        uuid = player.getUniqueId();
        gp = PlayerLagCompensation.INSTANCE.getGrimPlayer(player);
        lastLocation = player.getLocation();
        lastLastLocation = player.getLocation();
        lastLastLastLocation = player.getLocation();
        lastRelMove = player.getLocation().clone().zero();
        lastLastRelMove = player.getLocation().clone().zero();
        v = player.getLocation().clone().zero();
        a = player.getLocation().clone().zero();
        desyncLoc = player.getLocation();
        desyncLastLoc = player.getLocation();
        desyncV = player.getLocation().clone().zero();
        desyncA = player.getLocation().clone().zero();
    }

    public void updateLocation(Location location, boolean ground){
        if(location.equals(lastLocation)){
            return;
        }
        if(gp != null){
            if(kbDesync && lastVelocity == null && ground && gp.checkManager.getKnockbackHandler().getFutureKnockback().getFirst() == null){
                kbDesync = false;
                //gp.bukkitPlayer.sendMessage("resync");
            }
        }else {
            kbDesync = false;
        }

        lastLastLastLocation = lastLastLocation;
        lastLastLocation = lastLocation;
        lastLocation = location;
        lastLastRelMove = lastRelMove;
        lastRelMove = this.subtractRots(lastLocation.clone().subtract(lastLastLocation), lastLastLocation);
        if(!kbDesync && lastVelocity != null && gp != null && gp.checkManager.getKnockbackHandler().getFutureKnockback().getFirst() != null){
            kbDesync = true;
            desyncV = lastLastRelMove.clone();
            desyncLastLoc = lastLastLocation.clone();
            desyncLoc = location.clone();
            gp.bukkitPlayer.sendMessage("desync");
        }
        a = this.subtractRots(lastRelMove.clone().subtract(v), v);
        v = lastRelMove.clone();

        this.lastLastGround = this.lastGround;
        this.lastGround = this.ground;
        this.ground = ground;
        this.sprinting = Bukkit.getPlayer(uuid).isSprinting();
        if(gp != null){
            this.sprinting = gp.isSprinting;
        }

        if(kbDesync){
            Location lastDesyncV = desyncV.clone();
            if(gp != null){
                boolean flag = gp.likelyKB != null || gp.firstBreadKB != null;
                if(flag){
                    desyncV = gp.clientVelocity.clone().toLocation(gp.bukkitPlayer.getWorld());
                    kbDesync = false;
                    //gp.bukkitPlayer.sendMessage("resync");
                    lastVelocity = null;
                }
                //gp.bukkitPlayer.sendMessage("realY: " + gp.clientVelocity.getY() + ", kb: " + flag);
                //gp.bukkitPlayer.getWorld().spawnParticle(Particle.REDSTONE, desyncLoc, 0, 0.0001, 0.0, 255.0 / 255, 1.0);
            }
            desyncV = this.subtractRots(desyncLoc.clone().subtract(desyncLastLoc), desyncLastLoc.clone());
            desyncA = this.subtractRots(lastDesyncV.subtract(desyncV), desyncV);
        }else {
            //desyncLastLoc = desyncLoc.clone();
            //desyncLoc = location.clone();
            //desyncV = v.clone();
            desyncA = a.clone();
            this.desyncGround = ground;
        }

        if(ground){
            a.setY(Math.max(0, a.getY()));
            v.setY(Math.max(0, v.getY()));
        }

        calculatedLocations.clear();
    }

    public void teleport(Location location){
        lastLocation = location;
        lastLastLocation = location;
        lastLastLastLocation = location;
        lastRelMove = location.clone().zero();
        lastLastRelMove = location.clone().zero();
        v = location.clone().zero();
        a = location.clone().zero();
        desyncLoc = location;
        desyncLastLoc = location;
        desyncV = location.clone().zero();
        desyncA = location.clone().zero();
        entryMap.clear();
    }

    public Pair<Location,Boolean> predictLocation(int ticks){
        ticks = Math.abs(ticks);
        if(PlayerLagCompensation.INSTANCE.getConfigManager().maxPredictTicks != -1){
            ticks = Math.max(ticks, PlayerLagCompensation.INSTANCE.getConfigManager().maxPredictTicks);
        }

        if(this.gp == null){
            Player player = Bukkit.getPlayer(uuid);
            return new Pair<>(player.getLocation(), player.isOnGround());
        }else if(gp.bukkitPlayer.isFlying() || gp.bukkitPlayer.getGameMode() == GameMode.SPECTATOR){
            Player player = Bukkit.getPlayer(uuid);
            return new Pair<>(player.getLocation(), player.isOnGround());
        }

        if(calculatedLocations.containsKey(ticks)){
            return calculatedLocations.get(ticks);
        }

        Location preLoc = kbDesync ? desyncLoc.clone() : lastLocation.clone();
        Location preV = kbDesync ? desyncV.clone() : v.clone();
        Location preA = a.clone();
        boolean preGround = kbDesync ? desyncGround : this.ground;

        if(this.ground){
            preV.setY(Math.max(0, preV.getY()));
        }

        if(ticks == 0){
            desyncLastLoc = desyncLoc.clone();
            desyncLoc = preLoc.clone();
            desyncV = preV.clone();
            desyncGround = preGround;
            if(lastVelocity != null && lastVelocity.delayTicks == 0){
                preV.add(lastVelocity.velocity);
                desyncV = preV.clone();
                preLoc.add(desyncV);
                lastVelocity = null;
            }
        }

        if(kbDesync && lastVelocity != null && lastVelocity.delayTicks == 0){
            preV.add(lastVelocity.velocity);
            lastVelocity = null;
        }

        for(int i = 0; i < ticks; i++){

            if(kbDesync && lastVelocity != null){
                if(lastVelocity.delayTicks == i + 1){
                    preV.add(lastVelocity.velocity);
                    lastVelocity.delayTicks--;
                    if(lastVelocity.delayTicks == 0){
                        lastVelocity = null;
                    }
                }
            }
            Location calPreV = preV.clone();

            Location inputDirection = preV.clone().setDirection(preV.toVector());
            sprinting = sprinting && (PlayerLagCompensation.INSTANCE.getConfigManager().sprintTicks == -1 || PlayerLagCompensation.INSTANCE.getConfigManager().sprintTicks >= i + 1);
            double d0 = sprinting ? Math.sin((inputDirection.getYaw()) * 0.017453292f): 0;
            double d1 = sprinting ? Math.cos((inputDirection.getYaw()) * 0.017453292f): 0;

            if(preGround){
                if(preV.getX() != 0){
                    calPreV.setX(preV.getX() * 0.6 * 0.91 - (sprinting ? 0.1 * 1.3 * d0: 0.0));
                }
                if(preV.getZ() != 0){
                    calPreV.setZ(preV.getZ() * 0.6 * 0.91 + (sprinting ? 0.1 * 1.3 * d1: 0.0));
                }
            }else {
                if(preV.getX() != 0){
                    calPreV.setX(preV.getX() * 0.91  - (sprinting ? 0.02 * 1.3 * d0: 0.0));
                }
                if(preV.getY() != 0){
                    calPreV.setY((preV.getY() - 0.08) * 0.98);
                }
                if(preV.getZ() != 0){
                    calPreV.setZ(preV.getZ() * 0.91 + (sprinting ? 0.02 * 1.3 * d1: 0.0));
                }
            }

            preA = this.subtractRots(calPreV.clone().subtract(preV), preV);

            preV.setX(calPreV.getX());
            preV.setY(calPreV.getY());
            preV.setZ(calPreV.getZ());

            if(gp != null) {
                List<SimpleCollisionBox> collisions = new ArrayList<>();
                SimpleCollisionBox box = GetBoundingBox.getBoundingBoxFromPosAndSizeRaw(preLoc.getX(), preLoc.getY(), preLoc.getZ(), 0.6f, 1.8f);
                Collisions.getCollisionBoxes(this.gp, box.copy().expandToCoordinate(preV.getX(), preV.getY(), preV.getZ()), collisions, false);
                Vector vec = Collisions.collideBoundingBoxLegacy(new Vector(preV.getX(), preV.getY(), preV.getZ()), box.copy(), collisions, Arrays.asList(Collisions.Axis.Y, Collisions.Axis.X, Collisions.Axis.Z));

                this.addRots(preLoc.add(vec), preV);

                if(preV.getX() != vec.getX()){
                    preA.setX(0);
                    preV.setX(0);
                    xCollide = true;
                }else {
                    xCollide = false;
                }
                if(preV.getY() != vec.getY()){
                    if(preV.getY() < 0) {
                        preGround = true;
                    }else {
                        preGround = false;
                    }
                    preA.setY(0);
                    preV.setY(0);
                }else if(preV.getY() > 0){
                    preGround = false;
                }
                if(preV.getZ() != vec.getZ()){
                    preA.setZ(0);
                    preV.setZ(0);
                    zCollide = true;
                }else {
                    zCollide = false;
                }

            }else {
                this.addRots(preLoc.add(preV), preV);
            }

            if(kbDesync || true) {
                //preLoc.getWorld().spawnParticle(Particle.REDSTONE, preLoc, 0, 0.0001, 255.0 / 255, 0.0, 1.0);
            }
            calculatedLocations.put(i + 1, new Pair<>(preLoc.clone(), preGround));
            if(i == 0){
                desyncLastLoc = desyncLoc.clone();
                desyncLoc = preLoc.clone();
                desyncV = preV.clone();
                desyncGround = preGround;
            }
        }
        if(kbDesync || true) {
            //preLoc.getWorld().spawnParticle(Particle.REDSTONE, preLoc, 0, 255.0 / 255, 0.0, 0.0, 1.0);
            if(gp != null){
                //gp.bukkitPlayer.sendMessage("desyncY: " + preV.getY() + ", desyncLocY: " + preLoc.getY());
            }
        }
        return new Pair<>(preLoc.clone(), preGround);
    }

    public void removePlayer(){
        playerSet.remove(this.uuid);
    }

    private double round(double value){
        return Math.round(value * 1000) / 1000.0;
    }

    public class CompensationPlayerEntry{
        public UUID uuid;
        public CompensationPlayer cp;
        public Location lastSentLocation;
        public Location lastLastSentLocation;
        public int lastDelayTicks = 0;
        CompensationPlayerEntry(UUID uuid, CompensationPlayer cp){
            this.uuid = uuid;
            this.cp = cp;
            this.lastSentLocation = cp.lastLocation;
            this.lastLastSentLocation = cp.lastLocation;
        }

        public void updateSentLocation(Location vector, int delayTicks){
            lastLastSentLocation = lastSentLocation;
            lastSentLocation = vector;
            lastDelayTicks = delayTicks;
        }

        public Location getPredictionAcceleration(Location predict){
            Location lastPreV = lastSentLocation.clone().subtract(lastLastSentLocation);
            Location preV = predict.clone().subtract(lastSentLocation);
            return this.subtractRots(preV.clone().subtract(lastPreV), lastPreV);
        }

        private Location subtractRots(Location loc1, Location loc2) {
            loc1.setYaw(loc1.getYaw() - loc2.getYaw());
            loc1.setPitch(loc1.getPitch() - loc2.getPitch());
            return loc1;
        }

        private Location addRots(Location loc1, Location loc2) {
            loc1.setYaw(loc1.getYaw() + loc2.getYaw());
            loc1.setPitch(loc1.getPitch() + loc2.getPitch());
            return loc1;
        }

    }

    private Location subtractRots(Location loc1, Location loc2) {
        loc1.setYaw(loc1.getYaw() - loc2.getYaw());
        loc1.setPitch(loc1.getPitch() - loc2.getPitch());
        return loc1;
    }

    private Location addRots(Location loc1, Location loc2) {
        loc1.setYaw(loc1.getYaw() + loc2.getYaw());
        loc1.setPitch(loc1.getPitch() + loc2.getPitch());
        return loc1;
    }

}
