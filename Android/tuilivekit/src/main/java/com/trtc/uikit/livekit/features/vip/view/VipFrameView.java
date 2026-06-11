package com.trtc.uikit.livekit.features.vip.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;

import androidx.annotation.Nullable;

import com.trtc.uikit.livekit.features.vip.model.VipLevel;

/**
 * VipFrameView - Displays animated frame around user avatar
 * Supports different frame styles based on VIP level
 */
public class VipFrameView extends FrameLayout {
    private static final String TAG = "VipFrameView";

    private VipLevel vipLevel = VipLevel.NONE;
    private Paint framePaint;
    private Paint glowPaint;
    private ObjectAnimator glowAnimator;
    private float currentGlowAlpha = 0.3f;
    private boolean isAnimating = false;

    private int frameColor = 0xFFD4AF37; // Default gold
    private int glowRadius = 4;
    private float borderWidth = 3f;

    public VipFrameView(Context context) {
        super(context);
        init();
    }

    public VipFrameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VipFrameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        setupPaints();
    }

    private void setupPaints() {
        framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setStrokeWidth(borderWidth);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(borderWidth);
    }

    /**
     * Set VIP level and update frame appearance
     */
    public void setVipLevel(VipLevel vipLevel) {
        if (this.vipLevel == vipLevel) {
            return;
        }

        this.vipLevel = vipLevel;

        if (vipLevel != VipLevel.NONE) {
            updateFrameProperties();
            startGlowAnimation();
            invalidate();
        } else {
            stopGlowAnimation();
        }
    }

    /**
     * Update frame properties based on VIP level
     */
    private void updateFrameProperties() {
        if (vipLevel == VipLevel.NONE) {
            return;
        }

        frameColor = vipLevel.getFrameColorAccent();
        glowRadius = vipLevel.getFrameGlowRadius();
        borderWidth = 2 + (vipLevel.level * 0.5f);

        framePaint.setColor(frameColor);
        framePaint.setStrokeWidth(borderWidth);

        glowPaint.setColor(frameColor);
        glowPaint.setAlpha(128);
        glowPaint.setStrokeWidth(borderWidth);
    }

    /**
     * Start glow animation
     */
    private void startGlowAnimation() {
        if (isAnimating) {
            return;
        }

        isAnimating = true;

        glowAnimator = ObjectAnimator.ofFloat(this, "glowAlpha", 0.2f, 0.8f);
        glowAnimator.setDuration(1500);
        glowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        glowAnimator.setRepeatMode(ValueAnimator.REVERSE);
        glowAnimator.start();
    }

    /**
     * Stop glow animation
     */
    private void stopGlowAnimation() {
        isAnimating = false;
        if (glowAnimator != null) {
            glowAnimator.cancel();
            glowAnimator = null;
        }
    }

    /**
     * Set glow alpha for animation
     */
    public void setGlowAlpha(float alpha) {
        this.currentGlowAlpha = alpha;
        invalidate();
    }

    /**
     * Get current glow alpha
     */
    public float getGlowAlpha() {
        return currentGlowAlpha;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (vipLevel == VipLevel.NONE) {
            return;
        }

        int width = getWidth();
        int height = getHeight();
        float radius = Math.min(width, height) / 2f;

        // Draw glow effect
        glowPaint.setAlpha((int) (255 * currentGlowAlpha));
        for (int i = 0; i < glowRadius; i++) {
            float glowOffset = i * 1.5f;
            canvas.drawCircle(width / 2f, height / 2f, radius - borderWidth / 2 + glowOffset,
                    glowPaint);
        }

        // Draw frame border
        framePaint.setAlpha(255);
        canvas.drawCircle(width / 2f, height / 2f, radius - borderWidth / 2, framePaint);

        // Optional: Draw corner highlights for VIP 4+
        if (vipLevel.level >= 4) {
            drawCornerHighlights(canvas, width, height);
        }

        // Optional: Draw crown for VIP 5
        if (vipLevel == VipLevel.VIP_5) {
            drawCrownIndicator(canvas, width, height);
        }
    }

    /**
     * Draw corner highlights for premium VIP levels
     */
    private void drawCornerHighlights(Canvas canvas, int width, int height) {
        Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(frameColor);
        highlightPaint.setAlpha((int) (255 * currentGlowAlpha));

        float cornerSize = 8f;
        float offset = borderWidth;

        // Top-left
        canvas.drawRect(offset, offset, offset + cornerSize, offset + 1, highlightPaint);
        canvas.drawRect(offset, offset, offset + 1, offset + cornerSize, highlightPaint);

        // Top-right
        canvas.drawRect(width - offset - cornerSize, offset, width - offset, offset + 1, highlightPaint);
        canvas.drawRect(width - offset - 1, offset, width - offset, offset + cornerSize, highlightPaint);

        // Bottom-left
        canvas.drawRect(offset, height - offset - 1, offset + cornerSize, height - offset, highlightPaint);
        canvas.drawRect(offset, height - offset - cornerSize, offset + 1, height - offset, highlightPaint);

        // Bottom-right
        canvas.drawRect(width - offset - cornerSize, height - offset - 1, width - offset, height - offset,
                highlightPaint);
        canvas.drawRect(width - offset - 1, height - offset - cornerSize, width - offset, height - offset,
                highlightPaint);
    }

    /**
     * Draw crown indicator for VIP 5
     */
    private void drawCrownIndicator(Canvas canvas, int width, int height) {
        Paint crownPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        crownPaint.setColor(frameColor);
        crownPaint.setAlpha(200);
        crownPaint.setStrokeWidth(2);
        crownPaint.setStyle(Paint.Style.STROKE);

        float centerX = width / 2f;
        float topY = 5f;
        float crownSize = 10f;

        // Draw crown shape (simplified)
        // Left point
        canvas.drawLine(centerX - crownSize, topY + crownSize, centerX - crownSize / 2, topY,
                crownPaint);
        // Center point (highest)
        canvas.drawLine(centerX - crownSize / 2, topY, centerX, topY - 3, crownPaint);
        // Right point
        canvas.drawLine(centerX, topY - 3, centerX + crownSize / 2, topY, crownPaint);
        canvas.drawLine(centerX + crownSize / 2, topY, centerX + crownSize, topY + crownSize,
                crownPaint);

        // Base line
        canvas.drawLine(centerX - crownSize, topY + crownSize, centerX + crownSize,
                topY + crownSize, crownPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopGlowAnimation();
    }

    /**
     * Get current VIP level
     */
    public VipLevel getVipLevel() {
        return vipLevel;
    }

    /**
     * Check if frame is animating
     */
    public boolean isAnimating() {
        return isAnimating;
    }
}
