package com.trtc.uikit.livekit.features.vip.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.trtc.uikit.livekit.features.vip.model.VipState;
import com.trtc.uikit.livekit.features.vip.service.VIPService;

/**
 * StateAwareVipFrameView - VIP frame that always depends on live service state
 *
 * KEY DESIGN PRINCIPLE:
 * - This view does NOT store VIP state locally
 * - It ALWAYS observes the actual state from VIPService
 * - When VIP status changes, the frame immediately updates/disappears
 * - No hardcoded state - everything is driven by real-time service state
 */
public class StateAwareVipFrameView extends FrameLayout {
    private static final String TAG = "StateAwareVipFrameView";

    private String userId;
    private VipState currentState = VipState.createInactive();
    private Paint framePaint;
    private Paint glowPaint;
    private ObjectAnimator glowAnimator;
    private float currentGlowAlpha = 0.3f;
    private boolean isAnimating = false;
    private LifecycleOwner lifecycleOwner;

    private int frameColor = 0xFF808080;
    private int glowRadius = 2;
    private float borderWidth = 2f;

    // Observable state
    private OnStateChangedListener stateChangedListener;

    public interface OnStateChangedListener {
        void onVipActivated();
        void onVipExpired();
        void onVipStateChanged(VipState newState);
    }

    public StateAwareVipFrameView(Context context) {
        super(context);
        init();
    }

    public StateAwareVipFrameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StateAwareVipFrameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
     * Setup view with user ID and lifecycle owner
     * This subscribes to VIPService for real-time state updates
     */
    public void setupWithUser(String userId, LifecycleOwner lifecycleOwner) {
        this.userId = userId;
        this.lifecycleOwner = lifecycleOwner;

        // Subscribe to VIPService state
        if (lifecycleOwner != null) {
            VIPService vipService = VIPService.getInstance();
            vipService.getVipState(userId).observe(lifecycleOwner, new Observer<VipState>() {
                @Override
                public void onChanged(VipState newState) {
                    // This is called whenever VIP state changes in real-time
                    updateToState(newState);
                }
            });
        }
    }

    /**
     * Update frame based on state from VIPService
     * IMPORTANT: This is called automatically when service state changes
     */
    private void updateToState(VipState newState) {
        if (newState == null) {
            newState = VipState.createInactive();
        }

        VipState.Status oldStatus = currentState.status;
        VipState.Status newStatus = newState.status;

        currentState = newState;

        switch (newStatus) {
            case ACTIVE:
                // VIP is active - show frame with animation
                updateFrameProperties();
                startGlowAnimation();
                if (stateChangedListener != null && oldStatus != VipState.Status.ACTIVE) {
                    stateChangedListener.onVipActivated();
                }
                break;

            case EXPIRED:
            case INACTIVE:
                // VIP is not active - hide frame and stop animation
                stopGlowAnimation();
                if (stateChangedListener != null) {
                    stateChangedListener.onVipExpired();
                }
                break;
        }

        if (stateChangedListener != null) {
            stateChangedListener.onVipStateChanged(newState);
        }

        invalidate();
    }

    /**
     * Update frame visual properties based on VIP level
     */
    private void updateFrameProperties() {
        if (currentState.vipLevel != null) {
            frameColor = currentState.vipLevel.getFrameColorAccent();
            glowRadius = currentState.vipLevel.getFrameGlowRadius();
            borderWidth = 2 + (currentState.vipLevel.level * 0.5f);

            framePaint.setColor(frameColor);
            framePaint.setStrokeWidth(borderWidth);

            glowPaint.setColor(frameColor);
            glowPaint.setAlpha(128);
            glowPaint.setStrokeWidth(borderWidth);
        }
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

    public void setGlowAlpha(float alpha) {
        this.currentGlowAlpha = alpha;
        invalidate();
    }

    public float getGlowAlpha() {
        return currentGlowAlpha;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Only draw if VIP is ACTIVE
        if (currentState.status != VipState.Status.ACTIVE) {
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

        // Draw crown for VIP 5
        if (currentState.vipLevel != null && currentState.vipLevel.hasCrownAnimation()) {
            drawCrownIndicator(canvas, width, height);
        }
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

        // Draw crown shape
        canvas.drawLine(centerX - crownSize, topY + crownSize, centerX - crownSize / 2, topY,
                crownPaint);
        canvas.drawLine(centerX - crownSize / 2, topY, centerX, topY - 3, crownPaint);
        canvas.drawLine(centerX, topY - 3, centerX + crownSize / 2, topY, crownPaint);
        canvas.drawLine(centerX + crownSize / 2, topY, centerX + crownSize, topY + crownSize,
                crownPaint);
        canvas.drawLine(centerX - crownSize, topY + crownSize, centerX + crownSize,
                topY + crownSize, crownPaint);
    }

    /**
     * Get current state
     */
    public VipState getCurrentState() {
        return currentState;
    }

    /**
     * Set state change listener
     */
    public void setStateChangeListener(OnStateChangedListener listener) {
        this.stateChangedListener = listener;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopGlowAnimation();
    }
}
