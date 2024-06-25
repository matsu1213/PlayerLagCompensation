package net.azisaba.playerlagcompensation;

import org.bukkit.util.Vector;

public class CompensationVelocityData {
    public Vector velocity;
    public int delayTicks;

    public CompensationVelocityData(Vector velocity, int delayTicks) {
        this.velocity = velocity;
        this.delayTicks = delayTicks;
    }
}
