package net.azisaba.playerlagcompensation;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.data.Pair;
import ac.grim.grimac.utils.nmsutil.Collisions;
import ac.grim.grimac.utils.nmsutil.GetBoundingBox;
import org.bukkit.Bukkit;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
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
    public Vector lastVector;
    public Vector lastLastVector;
    public Vector lastLastLastVector;
    public Vector lastRelMove;
    public Vector lastLastRelMove;
    public Vector v;
    public Vector a;
    public boolean ground = false;
    public boolean lastGround = false;
    public boolean lastLastGround = false;
    public boolean xCollide = false;
    public boolean zCollide = false;
    public HashMap<UUID, Vector> lastSentVectorMap = new HashMap<>();
    public HashMap<UUID, Vector> lastLastSentVectorMap = new HashMap<>();
    CompensationPlayer(Player player){
        uuid = player.getUniqueId();
        gp = PlayerLagCompensation.INSTANCE.getGrimPlayer(player);
        lastVector = player.getLocation().toVector();
        lastLastVector = player.getLocation().toVector();
        lastLastLastVector = player.getLocation().toVector();
        lastRelMove = player.getLocation().toVector().clone().zero();
        lastLastRelMove = player.getLocation().toVector().clone().zero();
        v = player.getLocation().toVector().clone().zero();
        a = player.getLocation().toVector().clone().zero();
    }

    public void updateLocation(Vector location, boolean ground){
        lastLastLastVector = lastLastVector;
        lastLastVector = lastVector;
        lastVector = location;
        lastLastRelMove = lastRelMove;
        lastRelMove = lastVector.clone().subtract(lastLastVector);
        a = lastRelMove.clone().subtract(v);
        v = lastRelMove.clone();
        if(ground){
            a.setY(Math.max(0, a.getY()));
            v.setY(Math.max(0, v.getY()));
        }
        this.lastLastGround = this.lastGround;
        this.lastGround = this.ground;
        this.ground = ground;
    }

    public void teleport(Vector location){
        lastVector = location;
        lastLastVector = location;
        lastLastLastVector = location;
        lastRelMove = location.clone().zero();
        lastLastRelMove = location.clone().zero();
    }

    public Pair<Vector,Boolean> predictLocation(int ticks){
        Vector preLoc = lastVector.clone();
        Vector preV = v.clone();
        Vector preA = a.clone();
        boolean preGround = this.ground;
        boolean lastPreGround = this.lastGround;
        boolean lastLastPreGround = this.lastLastGround;
        boolean sprinting = Bukkit.getPlayer(uuid).isSprinting();
        if(gp != null){
            sprinting = gp.isSprinting;
        }
        if(this.ground){
            preV.setY(Math.max(0, preV.getY()));
        }
        for(int i = 0; i < ticks; i++){
            Vector culPreV = preV.clone();
            //double preAX = preV.getX();//-0.08 * preV.getX();
            //double preAY = -0.02 * preV.getY() - 0.0784;
            //double preAZ = preV.getZ();//0.08 * preV.getZ();
            if(preGround){
                if(preV.getX() != 0){
                    culPreV.setX(preV.getX() * 0.6 * 0.91 + 0.1 * (sprinting ? 1.3 : 1.0));
                }
                if(preV.getZ() != 0){
                    culPreV.setZ(preV.getZ() * 0.6 * 0.91 + 0.1 * (sprinting ? 1.3 : 1.0));
                }
            /*}else if(lastPreGround){
                if(preV.getX() != 0){
                    culPreV.setX(preV.getX() * 0.91 + 0.02 * (sprinting ? 1.3 : 1.0) + (sprinting ? 0.2 : 0));
                }
                if(preV.getY() != 0){
                    culPreV.setY((preV.getY() - 0.08) * 0.98);
                }
                if(preV.getZ() != 0){
                    culPreV.setZ(preV.getZ() * 0.91 + 0.02 * (sprinting ? 1.3 : 1.0) + (sprinting ? 0.2 : 0));
                }*/
            }else {
                if(preV.getX() != 0){
                    culPreV.setX(preV.getX() * 0.91 + 0.02 * (sprinting ? 1.3 : 1.0));
                }
                if(preV.getY() != 0){
                    culPreV.setY((preV.getY() - 0.08) * 0.98);
                }
                if(preV.getZ() != 0){
                    culPreV.setZ(preV.getZ() * 0.91 + 0.02 * (sprinting ? 1.3 : 1.0));
                }
            }

            /*
            if((lastPreGround && !preGround) || (lastLastPreGround && !lastPreGround && !preGround)){
                Player p = Bukkit.getPlayer(uuid);
                if(p.hasPotionEffect(PotionEffectType.JUMP)){
                    culPreV.setY(Math.min(culPreV.getY(), 0.42 + p.getPotionEffect(PotionEffectType.JUMP).getAmplifier() + 1) * 0.1);
                }else {
                    culPreV.setY(Math.min(culPreV.getY(), 0.42));
                }
            }
            */

            //preA.setX(preAX);
            //preA.setY(preAY);
            //preA.setZ(preAZ);
            //preV.add(preA);
            preA = culPreV.clone().subtract(preV);

            preV.setX(culPreV.getX());
            preV.setY(culPreV.getY());
            preV.setZ(culPreV.getZ());

            if(gp != null) {
                List<SimpleCollisionBox> collisions = new ArrayList<>();
                SimpleCollisionBox box = GetBoundingBox.getBoundingBoxFromPosAndSize(preLoc.getX(), preLoc.getY(), preLoc.getZ(), 0.6f, 1.8f);
                Collisions.getCollisionBoxes(this.gp, box.copy().expandToCoordinate(preV.getX(), preV.getY(), preV.getZ()), collisions, false);
                Vector vec = Collisions.collideBoundingBoxLegacy(new Vector(preV.getX(), preV.getY(), preV.getZ()), box.copy(), collisions, Arrays.asList(Collisions.Axis.Y, Collisions.Axis.X, Collisions.Axis.Z));

                preLoc.add(vec);

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
                preLoc.add(preV);
            }
        }
        return new Pair<>(preLoc, preGround);
    }

    public Vector getLastPredictedLocation(UUID uuid){
        return lastSentVectorMap.getOrDefault(uuid, lastLastVector);
    }

    public Vector getLastLastPredictedLocation(UUID uuid){
        return lastLastSentVectorMap.getOrDefault(uuid, lastLastLastVector);
    }

    public Vector getLastPredictionVelocity(UUID uuid, Vector lastPredicted){
        return lastPredicted.clone().subtract(this.getLastLastPredictedLocation(uuid));
    }

    public Vector getPredictionAcceleration(UUID uuid, Vector predict, Vector lastPredicted){
        Vector lastPreV = this.getLastPredictionVelocity(uuid, lastPredicted);
        Vector preV = predict.clone().subtract(lastPredicted);
        return preV.clone().subtract(lastPreV);
    }

    public void removePlayer(){
        playerSet.remove(this.uuid);
    }

    private double round(double value){
        return Math.round(value * 1000) / 1000.0;
    }

}
