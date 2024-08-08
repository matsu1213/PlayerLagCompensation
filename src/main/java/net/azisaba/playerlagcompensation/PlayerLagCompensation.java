package net.azisaba.playerlagcompensation;

import ac.grim.grimac.api.GrimAbstractAPI;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlayerLagCompensation extends JavaPlugin {

    static PlayerLagCompensation INSTANCE;
    private ConfigManager configManager;
    private BukkitEventListener packetListener;
    private PacketEventListener packetEventListener;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(true)
                .checkForUpdates(false);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        INSTANCE = this;
        packetListener = new BukkitEventListener();
        packetEventListener = new PacketEventListener();

        //Bukkit.getPluginCommand("compensatedcountdown").setExecutor(new CompensatedCountDownCommand());
        Bukkit.getPluginManager().registerEvents(packetListener, this);
        //Bukkit.getPluginManager().registerEvents(new CSCompensation(), this);

        Bukkit.getPluginCommand("togglecompensation").setExecutor(new ToggleCompensationCommand());
        Bukkit.getPluginCommand("togglevelocitycompensation").setExecutor(new ToggleCompensationCommand());

        PacketEvents.getAPI().getEventManager().registerListener(packetEventListener);
        PacketEvents.getAPI().init();

        configManager = new ConfigManager();

        //Bukkit.getScheduler().runTaskTimer(this, () -> {
        //    for(Player player : Bukkit.getOnlinePlayers()){
        //        ((CraftPlayer)player).getHandle().impulse = true;
        //    }
        //}, 0, 1);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        PacketEvents.getAPI().terminate();
    }

    public ConfigManager getConfigManager(){
        return configManager;
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
