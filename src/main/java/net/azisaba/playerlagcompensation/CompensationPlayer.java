package net.azisaba.playerlagcompensation;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.data.Pair;
import ac.grim.grimac.utils.nmsutil.Collisions;
import ac.grim.grimac.utils.nmsutil.GetBoundingBox;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
    }

    public void updateLocation(Location location, boolean ground){
        if(location.equals(lastLocation)){
            return;
        }

        calculatedLocations.clear();

        lastLastLastLocation = lastLastLocation;
        lastLastLocation = lastLocation;
        lastLocation = location;
        lastLastRelMove = lastRelMove;
        lastRelMove = this.subtractRots(lastLocation.clone().subtract(lastLastLocation), lastLastLocation);
        a = this.subtractRots(lastRelMove.clone().subtract(v), v);
        v = lastRelMove.clone();
        if(ground){
            a.setY(Math.max(0, a.getY()));
            v.setY(Math.max(0, v.getY()));
        }
        this.lastLastGround = this.lastGround;
        this.lastGround = this.ground;
        this.ground = ground;
        this.sprinting = Bukkit.getPlayer(uuid).isSprinting();
        if(gp != null){
            this.sprinting = gp.isSprinting;
        }
    }

    public void teleport(Location location){
        lastLocation = location;
        lastLastLocation = location;
        lastLastLastLocation = location;
        lastRelMove = location.clone().zero();
        lastLastRelMove = location.clone().zero();
    }

    public Pair<Location,Boolean> predictLocation(int ticks){
        ticks = Math.abs(ticks);

        if(calculatedLocations.containsKey(ticks)){
            return calculatedLocations.get(ticks);
        }

        Location preLoc = lastLocation.clone();
        Location preV = v.clone();
        Location preA = a.clone();
        boolean preGround = this.ground;
        boolean lastPreGround = this.lastGround;

        if(this.ground){
            preV.setY(Math.max(0, preV.getY()));
        }
        for(int i = 0; i < ticks; i++){
            Location calPreV = preV.clone();
            Location inputDirection = preV.clone().setDirection(preV.toVector());
            if(preGround){
                if(preV.getX() != 0){
                    calPreV.setX(preV.getX() * 0.6 * 0.91 + 0.1 * (sprinting ? 1.3 : 1.0) * Math.sin((inputDirection.getYaw() - 90) * Math.PI / 180));
                }
                if(preV.getZ() != 0){
                    calPreV.setZ(preV.getZ() * 0.6 * 0.91 + 0.1 * (sprinting ? 1.3 : 1.0) * Math.cos((inputDirection.getYaw() - 90) * Math.PI / 180));
                }
            }else {
                if(preV.getX() != 0){
                    calPreV.setX(preV.getX() * 0.91 + 0.02 * (sprinting ? 1.3 : 1.0) * Math.sin((inputDirection.getYaw() - 90) * Math.PI / 180));
                }
                if(preV.getY() != 0){
                    calPreV.setY((preV.getY() - 0.08) * 0.98);
                }
                if(preV.getZ() != 0){
                    calPreV.setZ(preV.getZ() * 0.91 + 0.02 * (sprinting ? 1.3 : 1.0) * Math.cos((inputDirection.getYaw() - 90) * Math.PI / 180));
                }
            }

            preA = this.subtractRots(calPreV.clone().subtract(preV), preV);

            preV.setX(calPreV.getX());
            preV.setY(calPreV.getY());
            preV.setZ(calPreV.getZ());

            if(gp != null) {
                List<SimpleCollisionBox> collisions = new ArrayList<>();
                SimpleCollisionBox box = GetBoundingBox.getBoundingBoxFromPosAndSize(preLoc.getX(), preLoc.getY(), preLoc.getZ(), 0.6f, 1.8f);
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
                        lastPreGround = preGround;
                        preGround = true;
                    }else {
                        lastPreGround = preGround;
                        preGround = false;
                    }
                    preA.setY(0);
                    preV.setY(0);
                }else if(preV.getY() > 0){
                    lastPreGround = preGround;
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

            calculatedLocations.put(i + 1, new Pair<>(preLoc.clone(), preGround));
        }
        return new Pair<>(preLoc, preGround);
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
        CompensationPlayerEntry(UUID uuid, CompensationPlayer cp){
            this.uuid = uuid;
            this.cp = cp;
            this.lastSentLocation = cp.lastLocation;
            this.lastLastSentLocation = cp.lastLocation;
        }

        public void updateSentLocation(Location vector){
            lastLastSentLocation = lastSentLocation;
            lastSentLocation = vector;
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
