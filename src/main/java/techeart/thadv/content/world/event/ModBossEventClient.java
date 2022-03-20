package techeart.thadv.content.world.event;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.UUID;

public class ModBossEventClient extends ModBossEvent
{
    private static final long LERP_DELAY = 1000L;
    private static final long LERP_TIME = 500L;

    protected long setTime;
    protected float targetProgress;

    public ModBossEventClient(UUID id, Component name) { this(id, name, 1.0f); }

    public ModBossEventClient(UUID id, Component name, float progress)
    {
        super(id, name);
        setTime = Util.getMillis();
        targetProgress = progress;
        this.progress = progress;
    }

    @Override
    public void setProgress(float value)
    {
        progress = getProgress();
        targetProgress = value;
        setTime = Util.getMillis();
    }

    @Override
    public float getProgress()
    {
        long timeSinceSet = Util.getMillis() - setTime;
        if(timeSinceSet < LERP_DELAY) return progress;
        timeSinceSet -= LERP_DELAY;
        float f = Mth.clamp((float)timeSinceSet / LERP_TIME, 0.0F, 1.0F);
        return Mth.lerp(f, progress, targetProgress);
    }

    public float getTargetProgress() { return targetProgress; }
}
