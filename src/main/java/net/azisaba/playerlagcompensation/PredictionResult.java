package net.azisaba.playerlagcompensation;

import org.bukkit.Location;

public class PredictionResult {
    public Location position;
    public boolean onGround;
    public CompensationPlayer.CompensationPlayerEntry entry;
    public Location v;

    public PredictionResult(Location position, boolean onGround, CompensationPlayer.CompensationPlayerEntry entry, Location v) {
        this.position = position;
        this.onGround = onGround;
        this.entry = entry;
        this.v = v;
    }
}
