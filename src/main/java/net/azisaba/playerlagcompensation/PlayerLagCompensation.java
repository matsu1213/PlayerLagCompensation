package net.azisaba.playerlagcompensation;

import ac.grim.grimac.api.GrimAbstractAPI;
import ac.grim.grimac.api.GrimUser;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketEvent;
import com.comphenix.protocol.ProtocolLibrary;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlayerLagCompensation extends JavaPlugin {

    static PlayerLagCompensation INSTANCE;
    private PacketListener packetListener;
    private PacketEventListener packetEventListener;

    @Override
    public void onEnable() {
        // Plugin startup logic
        INSTANCE = this;
        packetListener = new PacketListener();
        packetEventListener = new PacketEventListener();
        Bukkit.getPluginManager().registerEvents(packetListener, this);
        //ProtocolLibrary.getProtocolManager().addPacketListener(packetListener);

        PacketEvents.getAPI().getEventManager().registerListener(packetEventListener);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for(Player player : Bukkit.getOnlinePlayers()){
                ((CraftPlayer)player).getHandle().impulse = true;
            }
        }, 0, 1);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        //ProtocolLibrary.getProtocolManager().removePacketListener(packetListener);
        PacketEvents.getAPI().getEventManager().unregisterListener(packetEventListener);
    }

    public GrimPlayer getGrimPlayer(Player player) {
        RegisteredServiceProvider<GrimAbstractAPI> provider = Bukkit.getServicesManager().getRegistration(GrimAbstractAPI.class);
        if (provider != null) {
            GrimAbstractAPI api = provider.getProvider();
            return (GrimPlayer) api.getGrimUser(player);
        }
        return null;
    }
}
