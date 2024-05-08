package net.azisaba.playerlagcompensation;

import org.bukkit.Location;

public class PredictionResult {
    public Location position;
    public boolean onGround;
    public CompensationPlayer.CompensationPlayerEntry entry;
    public Location v;
    public int delayTicks;

    public PredictionResult(Location position, boolean onGround, CompensationPlayer.CompensationPlayerEntry entry, Location v, int delayTicks) {
        this.position = position;
        this.onGround = onGround;
        this.entry = entry;
        this.v = v;
        this.delayTicks = delayTicks;
    }
}
