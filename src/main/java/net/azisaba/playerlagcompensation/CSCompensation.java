package net.azisaba.playerlagcompensation;

import com.shampaggon.crackshot.events.WeaponShootEvent;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CSCompensation implements Listener {

    @EventHandler
    public void onShoot(WeaponShootEvent e){
        if(e.getProjectile() != null){
            int delayTicks = PlayerPingUtil.getPing(e.getPlayer().getUniqueId()) / 50;

            for (int i = 0; i < delayTicks; i++){
                ((CraftEntity) e.getProjectile()).getHandle().B_();
            }

        }
    }

}
