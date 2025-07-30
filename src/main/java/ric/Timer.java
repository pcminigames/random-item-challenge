package ric;

import java.util.function.Function;

import org.bukkit.scheduler.BukkitRunnable;

public class Timer extends BukkitRunnable {
    private int tickTimeLeft; // Infinite time when set to -1
    private final int tickPeriod;
    private final Function<Integer, Void> onTick;
    private final Function<Void, Void> onFinish;

    public Timer(int tickTotalTime, int tickPeriod, Function<Integer, Void> onTick, Function<Void, Void> onFinish) {
        this.tickTimeLeft = -1;

        if (tickTotalTime > 0 && tickPeriod > 0) {
            // Round up to the next multiple of interval
            this.tickTimeLeft = ((tickTotalTime + tickPeriod - 1) / tickPeriod) * tickPeriod;
        }

        this.onTick = onTick;
        this.onFinish = onFinish;
        this.tickPeriod = tickPeriod;
    }

    public static Timer loop(int tickPeriod, Function<Integer, Void> onTick) {
        return new Timer(-1, tickPeriod, onTick, null);
    }

    public static Timer after(int tickTime, Function<Void, Void> onFinish) {
        return new Timer(tickTime, tickTime, null, onFinish);
    }

    @Override
    public void run() {
        if (this.tickTimeLeft == 0) {
            if (this.onFinish != null) {this.onFinish.apply(null);}

            this.cancel();
            return;
        }

        this.tickTimeLeft -= this.tickPeriod;

        if (this.onTick != null) {this.onTick.apply(Math.abs(this.tickTimeLeft) / this.tickPeriod + 1);}
    }

    public void start() {
        this.runTaskTimer(PluginMain.getInstance(), 0, this.tickPeriod);
    }
}