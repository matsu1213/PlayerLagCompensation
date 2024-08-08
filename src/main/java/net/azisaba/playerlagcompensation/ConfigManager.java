package net.azisaba.playerlagcompensation;

import github.scarsz.configuralize.DynamicConfig;
import github.scarsz.configuralize.Language;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    public final DynamicConfig config;
    private final File configFile = new File(PlayerLagCompensation.INSTANCE.getDataFolder(), "config.yml");

    public int maxPredictTicks = -1;
    public int sprintTicks = 0;

    public ConfigManager(){
        PlayerLagCompensation.INSTANCE.getDataFolder().mkdirs();
        config = new DynamicConfig();
        config.addSource(PlayerLagCompensation.class, "config", configFile);
        reload();
    }

    public void reload(){
        config.setLanguage(Language.EN);
        try {
            config.saveAllDefaults(false);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save default config files", e);
        }
        try {
            config.loadAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config", e);
        }
        maxPredictTicks = config.getIntElse("max-predict-ticks", -1);
        ToggleVelocityCompensationCommand.shouldVelocityCompensate = config.getBooleanElse("predict-velocity", true);
        sprintTicks = config.getIntElse("max-sprint-ticks", 0);
    }

}
