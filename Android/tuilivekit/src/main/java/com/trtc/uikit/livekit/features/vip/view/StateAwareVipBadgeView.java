package com.trtc.uikit.livekit.features.vip.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.trtc.uikit.livekit.features.vip.model.VipState;
import com.trtc.uikit.livekit.features.vip.service.VIPService;

/**
 * StateAwareVipBadgeView - VIP badge that always reflects live service state
 *
 * This badge:
 * - Always shows current VIP status from VIPService
 * - Appears/disappears when VIP status changes
 * - Never caches state - always up-to-date
 */
public class StateAwareVipBadgeView extends View {
    private static final String TAG = "StateAwareVipBadgeView";

    private String userId;
    private VipState currentState = VipState.createInactive();
    private Paint badgePaint;
    private Paint textPaint;
    private float cornerRadius = 4f;
    private float paddingH = 8f;
    private float paddingV = 4f;
    private LifecycleOwner lifecycleOwner;

    public StateAwareVipBadgeView(Context context) {
        super(context);
        init();
    }

    public StateAwareVipBadgeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StateAwareVipBadgeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        setupPaints();
    }

    private void setupPaints() {
        badgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        badgePaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(12f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    /**
     * Setup with user ID
     * Subscribes to VIPService for real-time updates
     */
    public void setupWithUser(String userId, LifecycleOwner lifecycleOwner) {
        this.userId = userId;
        this.lifecycleOwner = lifecycleOwner;

        if (lifecycleOwner != null) {
            VIPService vipService = VIPService.getInstance();
            vipService.getVipState(userId).observe(lifecycleOwner, new Observer<VipState>() {
                @Override
                public void onChanged(VipState newState) {
                    updateToState(newState);
                }
            });
        }
    }

    /**
     * Update badge based on state from VIPService
     */
    private void updateToState(VipState newState) {
        if (newState == null) {
            newState = VipState.createInactive();
        }

        currentState = newState;

        if (newState.status == VipState.Status.ACTIVE) {
            badgePaint.setColor(newState.vipLevel.getFrameColorAccent());
            setVisibility(VISIBLE);
        } else {
            setVisibility(GONE);
        }

        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (currentState.status != VipState.Status.ACTIVE) {
            setMeasuredDimension(0, 0);
            return;
        }

        String text = getBadgeText();
        float textWidth = textPaint.measureText(text);
        int width = (int) (textWidth + paddingH * 2 + 2);
        int height = (int) (16 + paddingV * 2 + 2);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (currentState.status != VipState.Status.ACTIVE) {
            return;
        }

        int width = getWidth();
        int height = getHeight();

        // Draw badge background
        RectF rectF = new RectF(0, 0, width, height);
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, badgePaint);

        // Draw text
        String text = getBadgeText();
        float textY = height / 2f + (textPaint.getTextSize() / 4);
        canvas.drawText(text, width / 2f, textY, textPaint);

        // Draw highlight line for premium levels
        if (currentState.vipLevel != null && currentState.vipLevel.level >= 3) {
            Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            highlightPaint.setColor(0xFFFFFFFF);
            highlightPaint.setAlpha(150);
            canvas.drawLine(2, 1, width - 2, 1, highlightPaint);
        }
    }

    private String getBadgeText() {
        if (currentState.vipLevel == null) {
            return "";
        }

        switch (currentState.vipLevel) {
            case VIP_1:
                return "VIP";
            case VIP_2:
                return "VIP 2";
            case VIP_3:
                return "VIP 3";
            case VIP_4:
                return "VIP 4";
            case VIP_5:
                return "👑 VIP 5";
            default:
                return "";
        }
    }

    public VipState getCurrentState() {
        return currentState;
    }
}
