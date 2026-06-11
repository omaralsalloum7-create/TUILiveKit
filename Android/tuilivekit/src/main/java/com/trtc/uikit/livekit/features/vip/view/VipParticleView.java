package com.trtc.uikit.livekit.features.vip.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.View;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;

import androidx.annotation.Nullable;

import com.trtc.uikit.livekit.features.vip.model.VipLevel;

/**
 * ParticleView - Renders particle effects for VIP frames
 */
public class VipParticleView extends View {
    private static final String TAG = "VipParticleView";

    private VipLevel vipLevel = VipLevel.NONE;
    private Particle[] particles;
    private Paint particlePaint;
    private ObjectAnimator animationAnimator;
    private float animationProgress = 0f;
    private static final int PARTICLE_COUNT = 8;

    private static class Particle {
        float x, y;
        float vx, vy;
        float alpha;
        float size;

        Particle(float x, float y) {
            this.x = x;
            this.y = y;
            this.alpha = 1f;
            this.size = 4f;
        }

        void update(float centerX, float centerY, float radius) {
            x = centerX + (float) Math.cos((x - centerX) / radius) * radius;
            y = centerY + (float) Math.sin((y - centerY) / radius) * radius;
            alpha -= 0.02f;
        }
    }

    public VipParticleView(Context context) {
        super(context);
        init();
    }

    public VipParticleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VipParticleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        initializeParticles();
    }

    private void initializeParticles() {
        particles = new Particle[PARTICLE_COUNT];
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double angle = (Math.PI * 2 * i) / PARTICLE_COUNT;
            particles[i] = new Particle((float) Math.cos(angle) * 50, (float) Math.sin(angle) * 50);
        }
    }

    /**
     * Set VIP level and enable particles if applicable
     */
    public void setVipLevel(VipLevel vipLevel) {
        if (this.vipLevel == vipLevel) {
            return;
        }

        this.vipLevel = vipLevel;

        if (vipLevel.hasParticleEffects()) {
            startParticleAnimation();
            setVisibility(VISIBLE);
        } else {
            stopParticleAnimation();
            setVisibility(GONE);
        }
    }

    /**
     * Start particle animation
     */
    private void startParticleAnimation() {
        if (animationAnimator != null) {
            return;
        }

        animationAnimator = ObjectAnimator.ofFloat(this, "animationProgress", 0f, 1f);
        animationAnimator.setDuration(2000);
        animationAnimator.setRepeatCount(ValueAnimator.INFINITE);
        animationAnimator.setRepeatMode(ValueAnimator.RESTART);
        animationAnimator.start();
    }

    /**
     * Stop particle animation
     */
    private void stopParticleAnimation() {
        if (animationAnimator != null) {
            animationAnimator.cancel();
            animationAnimator = null;
        }
    }

    /**
     * Set animation progress
     */
    public void setAnimationProgress(float progress) {
        this.animationProgress = progress;
        invalidate();
    }

    /**
     * Get animation progress
     */
    public float getAnimationProgress() {
        return animationProgress;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (vipLevel == VipLevel.NONE || !vipLevel.hasParticleEffects()) {
            return;
        }

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) - 10;

        particlePaint.setColor(vipLevel.getFrameColorAccent());

        for (Particle particle : particles) {
            particle.update(centerX, centerY, radius);

            if (particle.alpha > 0) {
                particlePaint.setAlpha((int) (255 * particle.alpha * animationProgress));
                canvas.drawCircle(particle.x, particle.y, particle.size, particlePaint);
            }
        }

        // Reset particles if animation completes
        if (animationProgress >= 0.9f) {
            initializeParticles();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopParticleAnimation();
    }

    /**
     * Get current VIP level
     */
    public VipLevel getVipLevel() {
        return vipLevel;
    }
}
